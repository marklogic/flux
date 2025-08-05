/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read rows via Optic from MarkLogic and write them to JSON Lines files on a local filesystem, HDFS, or S3 using
 * <a href="https://spark.apache.org/docs/3.5.6/sql-data-sources-json.html">Spark's JSON support</a>.
 */
public interface JsonLinesFilesExporter extends Executor<JsonLinesFilesExporter> {

    JsonLinesFilesExporter from(Consumer<ReadRowsOptions> consumer);

    JsonLinesFilesExporter from(String opticQuery);

    JsonLinesFilesExporter to(Consumer<WriteJsonLinesFilesOptions> consumer);

    JsonLinesFilesExporter to(String path);

    interface WriteJsonLinesFilesOptions extends WriteFilesOptions<WriteJsonLinesFilesOptions> {
        WriteJsonLinesFilesOptions encoding(String encoding);

        WriteJsonLinesFilesOptions additionalOptions(Map<String, String> options);
    }
}
