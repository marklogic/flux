/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface AzureStorageOptions {

    /**
     * @since 1.4.0
     */
    AzureStorageOptions storageAccount(String storageAccount);

    /**
     * @since 1.4.0
     */
    AzureStorageOptions storageType(AzureStorageType storageType);

    /**
     * @since 1.4.0
     */
    AzureStorageOptions accessKey(String accessKey);

    /**
     * @since 1.4.0
     */
    AzureStorageOptions sasToken(String sasToken);

    /**
     * @since 1.4.0
     */
    AzureStorageOptions sharedKey(String sharedKey);

    /**
     * @since 1.4.0
     */
    AzureStorageOptions containerName(String containerName);
}
