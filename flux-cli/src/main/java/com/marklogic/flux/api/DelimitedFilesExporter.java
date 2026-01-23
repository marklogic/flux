/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to delimited text files on a local filesystem, HDFS, or S3
 * using <a href="https://spark.apache.org/docs/latest/sql-data-sources-csv.html">Spark's CSV support</a>.
 */
public interface DelimitedFilesExporter extends Executor<DelimitedFilesExporter> {

    DelimitedFilesExporter from(Consumer<ReadRowsOptions> consumer);

    DelimitedFilesExporter from(String opticQuery);

    DelimitedFilesExporter to(Consumer<WriteDelimitedFilesOptions> consumer);

    DelimitedFilesExporter to(String path);

    interface WriteDelimitedFilesOptions extends WriteFilesOptions<WriteDelimitedFilesOptions> {
        WriteDelimitedFilesOptions encoding(String encoding);

        WriteDelimitedFilesOptions additionalOptions(Map<String, String> options);
    }
}
