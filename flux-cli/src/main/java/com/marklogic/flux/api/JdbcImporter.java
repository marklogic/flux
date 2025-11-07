/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html">Spark's JDBC support</a>
 * and write JSON or XML documents to MarkLogic.
 */
public interface JdbcImporter extends Executor<JdbcImporter> {

    interface ReadJdbcOptions extends JdbcOptions<ReadJdbcOptions> {
        /**
         * Either this or {@link #table(String)} must be invoked.
         */
        ReadJdbcOptions query(String query);

        /**
         * Either this or {@link #query(String)} must be invoked.
         *
         * @since 1.4.0
         */
        ReadJdbcOptions table(String table);

        ReadJdbcOptions groupBy(String groupBy);

        ReadJdbcOptions aggregateColumns(String aggregationName, String... columns);

        /**
         * @since 2.0.0
         */
        ReadJdbcOptions orderAggregation(String aggregationName, String columnName, boolean ascending);
    }

    JdbcImporter from(Consumer<ReadJdbcOptions> consumer);

    JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
