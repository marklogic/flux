/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Defines a strategy for sanitizing the column names in a Dataset read from a structured data source - such as a
 * CSV file or a JDBC table - before the column values are used to construct JSON or XML documents. Sanitizing
 * column names can avoid problems with characters that are not supported by certain MarkLogic features or by
 * XML element names.
 *
 * @since 2.2.0
 */
public enum ColumnNameStrategy {

    /**
     * Column names are left as-is. This is the default, for backwards compatibility.
     */
    NONE,

    /**
     * Western European Latin letters are first folded to their closest ASCII letter(s) - e.g. {@code "é"} becomes
     * {@code "e"}, {@code "ö"} becomes {@code "o"}, and {@code "ß"} becomes {@code "ss"}. This is language-neutral
     * folding rather than a language-specific spelling convention; for example, {@code "ö"} does not become the
     * German digraph {@code "oe"}. Then, each run of one or more characters that is still not an ASCII letter or
     * digit - including existing underscores - is replaced with a single underscore, and any leading or trailing
     * underscores are then removed. For example, {@code "first. name"} becomes {@code "first_name"},
     * {@code "a__b"} becomes {@code "a_b"}, {@code "_id"} becomes {@code "id"}, and {@code "prénom"} becomes
     * {@code "prenom"}. Characters with no ASCII equivalent, including non-Latin scripts, are replaced with an
     * underscore just like other unsupported characters.
     */
    SIMPLE
}
