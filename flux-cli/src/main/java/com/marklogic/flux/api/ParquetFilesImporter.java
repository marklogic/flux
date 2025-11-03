/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read Parquet files from supported file locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-parquet.html">Spark's Parquet support</a>,
 * and write JSON or XML documents to MarkLogic.
 */
public interface ParquetFilesImporter extends Executor<ParquetFilesImporter> {

    ParquetFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    ParquetFilesImporter from(String... paths);

    ParquetFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
