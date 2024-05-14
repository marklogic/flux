package com.marklogic.newtool.api;

import java.util.Map;
import java.util.function.Consumer;

public interface JsonFilesImporter extends Executor<JsonFilesImporter> {

    interface ReadJsonFilesOptions extends ReadFilesOptions<ReadJsonFilesOptions> {
        ReadJsonFilesOptions jsonLines(Boolean value);

        ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions);
    }

    JsonFilesImporter readFiles(Consumer<ReadJsonFilesOptions> consumer);

    JsonFilesImporter readFiles(String... paths);

    JsonFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}
