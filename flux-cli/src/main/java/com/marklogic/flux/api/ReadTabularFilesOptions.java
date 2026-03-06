/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

/**
 * For objects that can import tabular files, where the data source supports additional options
 * that do not have dedicated methods in the importer interface.
 */
public interface ReadTabularFilesOptions extends ReadFilesOptions<ReadTabularFilesOptions> {

    ReadTabularFilesOptions additionalOptions(Map<String, String> options);

    /**
     * Filter columns from the input data. The specified columns will not be included in the constructed documents.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code drop("salary")}</li>
     * <li>{@code drop("status")}</li>
     * <li>{@code drop("age", "city")}</li>
     * </ul>
     * </p>
     *
     * @param columns the names of the columns to drop from the input data; these columns will not be included in the constructed documents
     * @return this importer instance
     * @since 2.1.0
     */
    ReadTabularFilesOptions drop(String... columns);

    /**
     * @deprecated since 2.1.0; use {@link StructuredDataImporter#groupBy(String, java.util.function.Consumer)} instead.
     */
    @Deprecated
    ReadTabularFilesOptions groupBy(String columnName);

    /**
     * @deprecated since 2.1.0; use {@link StructuredDataImporter.GroupByOptions#aggregateColumns(String, String...)} instead.
     */
    @Deprecated
    ReadTabularFilesOptions aggregateColumns(String aggregationName, String... columns);

    /**
     * @deprecated since 2.1.0; use {@link StructuredDataImporter.GroupByOptions#orderAggregation(String, String, boolean)} instead.
     * @since 2.0.0
     */
    @Deprecated
    ReadTabularFilesOptions orderAggregation(String aggregationName, String columnName, boolean ascending);

    ReadTabularFilesOptions uriIncludeFilePath(boolean value);
}
