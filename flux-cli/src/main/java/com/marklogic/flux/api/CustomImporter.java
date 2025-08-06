/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read data via a custom Spark connector or data source and write JSON or XML documents to MarkLogic.
 */
public interface CustomImporter extends Executor<CustomImporter> {

    interface CustomReadOptions {
        CustomReadOptions source(String source);

        CustomReadOptions additionalOptions(Map<String, String> additionalOptions);

        CustomReadOptions s3AddCredentials();

        CustomReadOptions s3AccessKeyId(String accessKeyId);

        CustomReadOptions s3SecretAccessKey(String secretAccessKey);

        CustomReadOptions s3Endpoint(String endpoint);
    }

    CustomImporter from(Consumer<CustomReadOptions> consumer);

    CustomImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
