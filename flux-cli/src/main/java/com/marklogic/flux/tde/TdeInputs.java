/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TdeInputs {

    interface Column {
        String getName();

        default String getVal() {
            return getName();
        }

        String getScalarType();
    }

    private final String schemaName;
    private final String viewName;
    private final Iterator<Column> columns;
    private final Map<String, String> namespaces = new HashMap<>();

    private static final String DEFAULT_CONTEXT = "/";

    private String uri;
    private String context = DEFAULT_CONTEXT;
    private String[] collections;
    private String[] directories;
    private String permissions;
    private boolean json = true; // Default to JSON TDE, can be set to false for XML TDE
    private boolean disabled;
    private String viewLayout;
    private Map<String, String> columnVals;
    private Map<String, String> columnTypes;
    private Map<String, String> columnDefaultValues;
    private Map<String, String> columnInvalidValues;
    private Map<String, String> columnReindexing;
    private Map<String, String> columnPermissions;
    private Map<String, String> columnCollations;
    private List<String> nullableColumns;

    public TdeInputs(String schemaName, String viewName, Iterator<Column> columns) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.columns = columns;
    }

    public TdeInputs withContext(String context) {
        if (context != null && !context.isEmpty()) {
            this.context = context;
        }
        return this;
    }

    public TdeInputs withUri(String uri) {
        if (uri != null && !uri.isEmpty()) {
            this.uri = uri;
        }
        return this;
    }

    public TdeInputs withJsonRootName(String jsonRootName) {
        if (jsonRootName != null && !jsonRootName.isEmpty()) {
            this.json = true;
            // Don't override the context if the user already specified one.
            if (DEFAULT_CONTEXT.equals(this.context)) {
                this.context = "/" + jsonRootName;
            }
        }
        return this;
    }

    public TdeInputs withXmlRootName(String xmlRootName, String namespace) {
        if (xmlRootName != null && !xmlRootName.isEmpty()) {
            this.json = false;
            // Don't override the context if the user already specified one.
            if (DEFAULT_CONTEXT.equals(this.context)) {
                if (namespace != null) {
                    this.namespaces.put("ns1", namespace);
                    this.context = "/ns1:" + xmlRootName;
                } else {
                    this.context = "/" + xmlRootName;
                }
            }
        }
        return this;
    }

    public TdeInputs withPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    public TdeInputs withCollections(String... collections) {
        this.collections = collections;
        return this;
    }

    public TdeInputs withDirectories(String... directories) {
        this.directories = directories;
        return this;
    }

    public TdeInputs withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public TdeInputs withViewLayout(String viewLayout) {
        this.viewLayout = viewLayout;
        return this;
    }

    public TdeInputs withColumnVals(Map<String, String> columnVals) {
        this.columnVals = columnVals;
        return this;
    }

    public TdeInputs withColumnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
        return this;
    }

    public TdeInputs withColumnDefaultValues(Map<String, String> columnDefaultValues) {
        this.columnDefaultValues = columnDefaultValues;
        return this;
    }

    public TdeInputs withColumnInvalidValues(Map<String, String> columnInvalidValues) {
        this.columnInvalidValues = columnInvalidValues;
        return this;
    }

    public TdeInputs withColumnReindexing(Map<String, String> columnReindexing) {
        this.columnReindexing = columnReindexing;
        return this;
    }

    public TdeInputs withColumnPermissions(Map<String, String> columnPermissions) {
        this.columnPermissions = columnPermissions;
        return this;
    }

    public TdeInputs withColumnCollations(Map<String, String> columnCollations) {
        this.columnCollations = columnCollations;
        return this;
    }

    public TdeInputs withNullableColumns(List<String> nullableColumns) {
        this.nullableColumns = nullableColumns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }

    public Iterator<Column> getColumns() {
        return columns;
    }

    public String getContext() {
        return context;
    }

    public String[] getCollections() {
        return collections;
    }

    public String[] getDirectories() {
        return directories;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public boolean isJson() {
        return json;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getUri() {
        return uri;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getViewLayout() {
        return viewLayout;
    }

    public Map<String, String> getColumnVals() {
        return columnVals;
    }

    public Map<String, String> getColumnTypes() {
        return columnTypes;
    }

    public Map<String, String> getColumnDefaultValues() {
        return columnDefaultValues;
    }

    public Map<String, String> getColumnInvalidValues() {
        return columnInvalidValues;
    }

    public Map<String, String> getColumnReindexing() {
        return columnReindexing;
    }

    public Map<String, String> getColumnPermissions() {
        return columnPermissions;
    }

    public Map<String, String> getColumnCollations() {
        return columnCollations;
    }

    public List<String> getNullableColumns() {
        return nullableColumns;
    }
}
