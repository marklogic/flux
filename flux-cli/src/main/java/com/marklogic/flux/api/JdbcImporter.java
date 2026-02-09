/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html">Spark's JDBC support</a>
 * and write JSON or XML documents to MarkLogic.
 */
public interface JdbcImporter extends StructuredDataImporter<JdbcImporter> {

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

        /**
         * @deprecated since 2.1.0; use {@link JdbcImporter#groupBy(String, java.util.function.Consumer)} instead.
         */
        @Deprecated
        ReadJdbcOptions groupBy(String groupBy);

        /**
         * @deprecated since 2.1.0; use {@link StructuredDataImporter.GroupByOptions#aggregateColumns(String, String...)} instead.
         */
        @Deprecated
        ReadJdbcOptions aggregateColumns(String aggregationName, String... columns);

        /**
         * @deprecated since 2.1.0; use {@link StructuredDataImporter.GroupByOptions#orderAggregation(String, String, boolean)} instead.
         * @since 2.0.0
         */
        @Deprecated
        ReadJdbcOptions orderAggregation(String aggregationName, String columnName, boolean ascending);
    }

    JdbcImporter from(Consumer<ReadJdbcOptions> consumer);

    JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
