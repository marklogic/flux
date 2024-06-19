/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
        ReadJdbcOptions query(String query);

        ReadJdbcOptions groupBy(String groupBy);

        ReadJdbcOptions aggregateColumns(String newColumnName, String... columns);
    }

    JdbcImporter from(Consumer<ReadJdbcOptions> consumer);

    JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
