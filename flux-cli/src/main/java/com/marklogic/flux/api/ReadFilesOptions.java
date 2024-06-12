package com.marklogic.flux.api;

public interface ReadFilesOptions<T extends ReadFilesOptions> {

    T paths(String... paths);

    T filter(String filter);

    T recursiveFileLookup(Boolean value);

    T abortOnReadFailure(Boolean value);

    T s3AddCredentials();

    T s3AccessKeyId(String accessKeyId);

    T s3SecretAccessKey(String secretAccessKey);

    T s3Endpoint(String endpoint);
}
