/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to a table using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html">Spark's JDBC support</a>.
 */
public interface JdbcExporter extends Executor<JdbcExporter> {

    interface WriteRowsOptions extends JdbcOptions<WriteRowsOptions> {
        WriteRowsOptions table(String table);

        WriteRowsOptions saveMode(SaveMode saveMode);
    }

    JdbcExporter from(Consumer<ReadRowsOptions> consumer);

    JdbcExporter from(String opticQuery);

    JdbcExporter to(Consumer<WriteRowsOptions> consumer);
}
