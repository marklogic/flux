package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

public interface CustomImporter extends Executor<CustomImporter> {

    interface CustomReadOptions {
        CustomReadOptions source(String source);
        CustomReadOptions additionalOptions(Map<String, String> additionalOptions);
        CustomReadOptions s3AddCredentials();
        CustomReadOptions s3Endpoint(String endpoint);
    }

    CustomImporter readData(Consumer<CustomReadOptions> consumer);

    CustomImporter writeData(Consumer<WriteStructuredDocumentsOptions> consumer);
}
