/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface ReadFilesOptions<T extends ReadFilesOptions> {

    T paths(String... paths);

    T filter(String filter);

    T recursiveFileLookup(boolean value);

    T abortOnReadFailure(boolean value);

    T s3AddCredentials();

    T s3AccessKeyId(String accessKeyId);

    T s3SecretAccessKey(String secretAccessKey);

    T s3Endpoint(String endpoint);

    /**
     * @since 1.4.0
     */
    T azureStorageAccount(String storageAccount);

    /**
     * @since 1.4.0
     */
    T azureStorageType(AzureStorageType storageType);

    /**
     * @since 1.4.0
     */
    T azureAccessKey(String accessKey);

    /**
     * @since 1.4.0
     */
    T azureSasToken(String sasToken);

    /**
     * @since 1.4.0
     */
    T azureSharedKey(String sharedKey);

    /**
     * @since 1.4.0
     */
    T azureContainerName(String containerName);
}
