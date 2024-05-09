package com.marklogic.newtool.api;

public interface WriteFilesOptions<T extends WriteFilesOptions> {

    T path(String path);

    T fileCount(Integer fileCount);

    T s3AddCredentials();

    T s3Endpoint(String endpoint);
}
