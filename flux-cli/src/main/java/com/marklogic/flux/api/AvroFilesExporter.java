package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to Avro files on a local filesystem, HDFS, or S3.
 */
public interface AvroFilesExporter extends Executor<AvroFilesExporter> {

    AvroFilesExporter from(Consumer<ReadRowsOptions> consumer);

    AvroFilesExporter from(String opticQuery);

    AvroFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    AvroFilesExporter to(String path);
}
