package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

public interface JsonFilesImporter extends Executor<JsonFilesImporter> {

    interface ReadJsonFilesOptions extends ReadFilesOptions<ReadJsonFilesOptions> {
        ReadJsonFilesOptions jsonLines(Boolean value);

        ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions);
    }

    JsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer);

    JsonFilesImporter from(String... paths);

    JsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
