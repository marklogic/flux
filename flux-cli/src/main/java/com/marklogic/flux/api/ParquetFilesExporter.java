package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3.
 */
public interface ParquetFilesExporter extends Executor<ParquetFilesExporter> {

    ParquetFilesExporter from(Consumer<ReadRowsOptions> consumer);

    ParquetFilesExporter from(String opticQuery);

    ParquetFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    ParquetFilesExporter to(String path);
}
