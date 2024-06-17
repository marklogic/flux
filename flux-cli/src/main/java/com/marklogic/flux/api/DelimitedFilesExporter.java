/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to delimited text files on a local filesystem, HDFS, or S3.
 */
public interface DelimitedFilesExporter extends Executor<DelimitedFilesExporter> {

    DelimitedFilesExporter from(Consumer<ReadRowsOptions> consumer);

    DelimitedFilesExporter from(String opticQuery);

    DelimitedFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    DelimitedFilesExporter to(String path);
}
