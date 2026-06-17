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
     * @since 2.1.1
     */
    default T s3AnonymousAccess() {
        // Default implementation for backwards compatibility.
        throw new UnsupportedOperationException("S3 anonymous access is not supported in this implementation");
    }

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
