package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read Parquet files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-parquet.html">Spark's Parquet support</a>,
 * with each row being written as a JSON or XML document in MarkLogic.
 */
public interface ParquetFilesImporter extends Executor<ParquetFilesImporter> {

    ParquetFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    ParquetFilesImporter from(String... paths);

    ParquetFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
