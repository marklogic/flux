/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
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
}
