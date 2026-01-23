/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

public interface CustomExportWriteOptions {

    CustomExportWriteOptions target(String target);

    CustomExportWriteOptions additionalOptions(Map<String, String> additionalOptions);

    CustomExportWriteOptions saveMode(SaveMode saveMode);

    CustomExportWriteOptions s3AddCredentials();

    /**
     * Enable use of the AWS profile credentials provider.
     *
     * @since 2.0.0
     */
    CustomExportWriteOptions s3UseProfile();

    CustomExportWriteOptions s3AccessKeyId(String accessKeyId);

    CustomExportWriteOptions s3SecretAccessKey(String secretAccessKey);

    CustomExportWriteOptions s3Endpoint(String endpoint);

    /**
     * @since 2.0.0
     */
    CustomExportWriteOptions s3Region(String region);

    /**
     * @since 1.4.0
     */
    CustomExportWriteOptions azureStorage(Consumer<AzureStorageOptions> consumer);
}
