/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
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
