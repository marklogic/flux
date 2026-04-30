/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface WriteFilesOptions<T extends WriteFilesOptions>  {

    T path(String path);

    T fileCount(int fileCount);

    T s3AddCredentials();

    /**
     * Enable use of the AWS profile credentials provider.
     *
     * @since 2.0.0
     */
    T s3UseProfile();

    /**
     * @since 2.2.0
     */
    T s3AnonymousAccess();

    T s3Endpoint(String endpoint);

    /**
     * @since 2.0.0
     */
    T s3Region(String region);

    T s3AccessKeyId(String accessKeyId);

    T s3SecretAccessKey(String secretAccessKey);

    /**
     * @since 1.4.0
     */
    T azureStorage(Consumer<AzureStorageOptions> consumer);
}
