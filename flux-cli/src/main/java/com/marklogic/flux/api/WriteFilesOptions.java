/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface WriteFilesOptions<T extends WriteFilesOptions> {

    T path(String path);

    T fileCount(int fileCount);

    T s3AddCredentials();

    T s3Endpoint(String endpoint);

    T s3AccessKeyId(String accessKeyId);

    T s3SecretAccessKey(String secretAccessKey);
}
