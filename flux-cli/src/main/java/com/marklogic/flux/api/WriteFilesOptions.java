package com.marklogic.flux.api;

public interface WriteFilesOptions<T extends WriteFilesOptions> {

    T path(String path);

    T fileCount(Integer fileCount);

    T s3AddCredentials();

    T s3Endpoint(String endpoint);

    T s3AccessKeyId(String accessKeyId);

    T s3SecretAccessKey(String secretAccessKey);
}
