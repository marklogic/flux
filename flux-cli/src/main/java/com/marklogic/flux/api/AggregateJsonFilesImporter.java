/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read either JSON Lines files or files containing arrays of JSON objects from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-json.html">Spark's JSON support</a>,
 * and write each object as a JSON document to MarkLogic.
 */
public interface AggregateJsonFilesImporter extends Executor<AggregateJsonFilesImporter> {

    interface ReadJsonFilesOptions extends ReadFilesOptions<ReadJsonFilesOptions> {
        /**
         * @param value set to true to read JSON Lines files. Defaults to reading files that either contain an array
         *              of JSON objects or a single JSON object.
         */
        ReadJsonFilesOptions jsonLines(boolean value);

        /**
         * @since 1.1.2
         */
        ReadJsonFilesOptions jsonLinesRaw(boolean value);

        ReadJsonFilesOptions encoding(String encoding);

        ReadJsonFilesOptions uriIncludeFilePath(boolean value);

        ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions);
    }

    AggregateJsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer);

    AggregateJsonFilesImporter from(String... paths);

    AggregateJsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
