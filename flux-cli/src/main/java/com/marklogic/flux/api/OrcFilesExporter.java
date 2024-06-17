/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to ORC files on a local filesystem, HDFS, or S3.
 */
public interface OrcFilesExporter extends Executor<OrcFilesExporter> {

    OrcFilesExporter from(Consumer<ReadRowsOptions> consumer);

    OrcFilesExporter from(String opticQuery);

    OrcFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    OrcFilesExporter to(String path);
}
