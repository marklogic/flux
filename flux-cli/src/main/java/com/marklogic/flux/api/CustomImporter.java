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

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureStorageAccount(String storageAccount);

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureStorageType(AzureStorageType storageType);

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureAccessKey(String accessKey);

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureSasToken(String sasToken);

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureSharedKey(String sharedKey);

        /**
         * @since 1.4.0
         */
        CustomReadOptions azureContainerName(String containerName);
    }

    CustomImporter from(Consumer<CustomReadOptions> consumer);

    CustomImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
