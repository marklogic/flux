/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read JSON files, including JSON Lines files, from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-json.html">Spark's JSON support</a>,
 * and write each object as a JSON document to MarkLogic.
 */
public interface JsonFilesImporter extends Executor<JsonFilesImporter> {

    interface ReadJsonFilesOptions extends ReadFilesOptions<ReadJsonFilesOptions> {
        ReadJsonFilesOptions jsonLines(Boolean value);

        ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions);
    }

    JsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer);

    JsonFilesImporter from(String... paths);

    JsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
