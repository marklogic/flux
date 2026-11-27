/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the column references that a user provides - via options such as {@code --group-by} and
 * {@code --aggregate} - against the top-level column names of a dataset. Spark parses a column reference as an
 * identifier, so a name containing a period is read as a path to a nested field and a name containing a space cannot
 * be used in a SQL expression unless the name is quoted with backticks. A reference that matches a top-level column
 * name is therefore quoted so that Spark treats it as a literal name; any other reference - such as a path to a
 * field in a struct column - is left for Spark to parse.
 * <p>
 * When column names have been sanitized, a column can be referenced by either its sanitized name or its original name.
 * </p>
 * Decoupled from the Spark Dataset API so that it can be easily unit tested.
 */
class ColumnReferences {

    private final Set<String> columnNames;
    private final Map<String, String> columnNamesByReference = new HashMap<>();
    private final boolean caseSensitive;

    /**
     * For a dataset whose column names have not been sanitized and that is not case-sensitive.
     */
    ColumnReferences(String[] columnNames) {
        this(columnNames, columnNames, false);
    }

    /**
     * @param columnNames         the names of the top-level columns in the dataset
     * @param originalColumnNames the names that those columns had, in the same order, before they were sanitized
     * @param caseSensitive       whether Spark is resolving column names in a case-sensitive manner, which it does
     *                            not do by default; a reference must be matched to a column in the same manner
     */
    ColumnReferences(String[] columnNames, String[] originalColumnNames, boolean caseSensitive) {
        this.columnNames = new LinkedHashSet<>(Arrays.asList(columnNames));
        this.caseSensitive = caseSensitive;
        for (int i = 0; i < columnNames.length; i++) {
            columnNamesByReference.put(toKey(originalColumnNames[i]), columnNames[i]);
        }
        // Added last so that the name of a column takes precedence over the original name of another column.
        for (String columnName : columnNames) {
            columnNamesByReference.put(toKey(columnName), columnName);
        }
    }

    /**
     * @return the given name quoted with backticks, such that Spark will treat it as a literal column or field name
     */
    static String quote(String name) {
        return "`" + name.replace("`", "``") + "`";
    }

    /**
     * @return the name of the top-level column that the given reference identifies, or the reference itself if it
     * does not identify a top-level column
     */
    String resolve(String reference) {
        if (reference == null) {
            // Left for the caller to handle as it did before references were resolved - e.g. Spark ignores it in "drop".
            return null;
        }
        String columnName = findColumnName(reference);
        // A user may have already quoted a column name, as that is required in a "where" expression.
        if (columnName == null && reference.length() > 2 && reference.startsWith("`") && reference.endsWith("`")) {
            columnName = findColumnName(reference.substring(1, reference.length() - 1).replace("``", "`"));
        }
        return columnName != null ? columnName : reference;
    }

    private String findColumnName(String name) {
        return columnNames.contains(name) ? name : columnNamesByReference.get(toKey(name));
    }

    private String toKey(String name) {
        return caseSensitive ? name : name.toLowerCase(Locale.ROOT);
    }

    /**
     * @return an expression for the given reference that is safe to pass to Spark - e.g. via {@code functions.col}
     * or as part of a SQL expression. A reference to a top-level column is quoted. In a path to a field in a struct
     * column, the name of the column is quoted and the rest of the path is left for Spark to parse. Any other
     * reference is returned as-is.
     */
    String toExpression(String reference) {
        if (reference == null) {
            return null;
        }
        String columnName = resolve(reference);
        if (columnNames.contains(columnName)) {
            return quote(columnName);
        }

        if (reference.startsWith("`")) {
            // The user has quoted the first part of a path, which is how Spark expects a struct column whose name
            // has a period in it to be referenced. The rest of the path keeps the quoting that the user gave it.
            final int closingIndex = findClosingBacktick(reference);
            if (closingIndex > 1 && reference.startsWith(".", closingIndex + 1)) {
                String structColumnName = findColumnName(reference.substring(1, closingIndex).replace("``", "`"));
                if (structColumnName != null) {
                    return quote(structColumnName) + reference.substring(closingIndex + 1);
                }
            }
            return reference;
        }

        // Look for the longest column name that the reference starts with, as a column name may itself have a period.
        String structColumnName = null;
        int pathIndex = -1;
        for (Map.Entry<String, String> entry : columnNamesByReference.entrySet()) {
            final int length = entry.getKey().length();
            if (length > pathIndex && reference.length() > length && reference.charAt(length) == '.'
                && reference.regionMatches(!caseSensitive, 0, entry.getKey(), 0, length)) {
                structColumnName = entry.getValue();
                pathIndex = length;
            }
        }
        return structColumnName != null ? quote(structColumnName) + reference.substring(pathIndex) : reference;
    }

    /**
     * @return the index of the backtick that closes the quoted name that the given reference starts with, or -1 if
     * there isn't one. A doubled backtick is an escaped backtick within the name.
     */
    private static int findClosingBacktick(String reference) {
        for (int i = 1; i < reference.length(); i++) {
            if (reference.charAt(i) == '`') {
                if (reference.startsWith("`", i + 1)) {
                    i++;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }
}
