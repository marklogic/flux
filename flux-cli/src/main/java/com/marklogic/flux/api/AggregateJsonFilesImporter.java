/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read either JSON Lines files or files containing arrays of JSON objects from supported file locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-json.html">Spark's JSON support</a>,
 * and write each object as a JSON document to MarkLogic.
 */
public interface AggregateJsonFilesImporter extends Executor<AggregateJsonFilesImporter> {

    interface ReadJsonFilesOptions extends ReadFilesOptions<ReadJsonFilesOptions> {
        /**
         * @param value set to true to read JSON Lines files. Defaults to reading files that either contain an array
         *              of JSON objects or a single JSON object.
         * @deprecated since 1.1.2; use {@code jsonLines()} instead.
         */
        @SuppressWarnings("java:S1133") // Telling Sonar we don't need a reminder to remove this some day.
        @Deprecated(since = "1.1.2", forRemoval = true)
        ReadJsonFilesOptions jsonLines(boolean value);

        /**
         * Call this to read JSON Lines files. Otherwise, defaults to reading files that either contain an array of
         * JSON objects or a single JSON object.
         *
         * @since 1.1.2
         */
        ReadJsonFilesOptions jsonLines();

        /**
         * Call this to read JSON Lines files "as is", without any alteration to the documents associated with each
         * line.
         *
         * @since 1.1.2
         */
        ReadJsonFilesOptions jsonLinesRaw();

        ReadJsonFilesOptions encoding(String encoding);

        ReadJsonFilesOptions uriIncludeFilePath(boolean value);

        ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions);
    }

    AggregateJsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer);

    AggregateJsonFilesImporter from(String... paths);

    AggregateJsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
