/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3 using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-parquet.html">Spark's Parquet support</a>.
 */
public interface ParquetFilesExporter extends Executor<ParquetFilesExporter> {

    ParquetFilesExporter from(Consumer<ReadRowsOptions> consumer);

    ParquetFilesExporter from(String opticQuery);

    ParquetFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    ParquetFilesExporter to(String path);
}
