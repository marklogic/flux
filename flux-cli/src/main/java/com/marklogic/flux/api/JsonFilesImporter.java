package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read JSON files, including JSON Lines files, from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-json.html">Spark's JSON support </a>,
 * with each object being written as a JSON document in MarkLogic.
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
