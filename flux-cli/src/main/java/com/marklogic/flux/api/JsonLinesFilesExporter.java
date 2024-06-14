package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to JSON Lines files on a local filesystem, HDFS, or S3.
 */
public interface JsonLinesFilesExporter extends Executor<JsonLinesFilesExporter> {

    JsonLinesFilesExporter from(Consumer<ReadRowsOptions> consumer);

    JsonLinesFilesExporter from(String opticQuery);

    JsonLinesFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    JsonLinesFilesExporter to(String path);
}
