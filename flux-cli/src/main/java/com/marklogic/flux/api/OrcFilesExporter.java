/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to ORC files on a local filesystem, HDFS, or S3 using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-orc.html">Spark's ORC support</a>.
 */
public interface OrcFilesExporter extends Executor<OrcFilesExporter> {

    OrcFilesExporter from(Consumer<ReadRowsOptions> consumer);

    OrcFilesExporter from(String opticQuery);

    OrcFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    OrcFilesExporter to(String path);
}
