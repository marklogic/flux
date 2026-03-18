/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Base interface for importers that read structured data with column-based schemas. Extends the base
 * {@link Executor} interface with methods for transforming structured data via filtering, grouping, and aggregation.
 * <p>
 * These transformation operations occur after data is read from the source and before it is written to MarkLogic:
 * </p>
 * <pre>
 * READ → [groupBy/limit/repartition] → WRITE
 * </pre>
 *
 * @param <T> the type of the importer implementation
 * @since 2.1.0
 */
public interface StructuredDataImporter<T extends StructuredDataImporter<T>> extends Executor<T> {

    /**
     * Options for configuring aggregation behavior when grouping rows.
     *
     * @since 2.1.0
     */
    interface GroupByOptions<T extends GroupByOptions<T>> {
        /**
         * Define an aggregation of multiple columns into a new column. Each aggregation creates a new column containing
         * an array of values from the specified columns.
         *
         * @param aggregationName the name of the new column to create
         * @param columns         the names of the columns to aggregate into an array
         * @return this options instance
         * @since 2.1.0
         */
        T aggregateColumns(String aggregationName, String... columns);

        /**
         * Specify ordering for an aggregation produced by {@link #aggregateColumns(String, String...)}. The column name
         * must be one of the columns in the corresponding aggregation.
         *
         * @param aggregationName the name of the aggregation to order
         * @param columnName      the name of the column to order by
         * @param ascending       true for ascending order, false for descending
         * @return this options instance
         * @since 2.1.0
         */
        T orderAggregation(String aggregationName, String columnName, boolean ascending);
    }

    /**
     * Filter rows using a SQL-like WHERE expression before constructing documents.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code where("size < 10")}</li>
     * <li>{@code where("status = 'active'")}</li>
     * <li>{@code where("age > 21 AND city = 'Boston'")}</li>
     * </ul>
     * </p>
     *
     * @param expression a SQL-like WHERE clause expression
     * @return this importer instance
     * @since 2.1.0
     */
    T where(String expression);

    /**
     * Group rows by the specified column name before constructing documents, with options for aggregating and
     * ordering columns.
     *
     * @param columnName the name of the column to group by
     * @param consumer   a consumer to configure aggregation options
     * @return this importer instance
     * @since 2.1.0
     */
    T groupBy(String columnName, Consumer<GroupByOptions<?>> consumer);
}
