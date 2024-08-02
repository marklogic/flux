/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.impl.S3Params;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Supplier;

public abstract class WriteFilesParams<T extends WriteFilesOptions> implements Supplier<Map<String, String>>, WriteFilesOptions<T> {

    @CommandLine.Option(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @CommandLine.Mixin
    private S3Params s3Params = new S3Params();

    @CommandLine.Option(names = "--file-count", description = "Specifies how many files should be written; also an alias for '--repartition'.")
    protected int fileCount;

    public String getPath() {
        return path;
    }

    public S3Params getS3Params() {
        return s3Params;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void validatePath() {
        if (path == null || path.trim().length() == 0) {
            throw new FluxException("Must specify a file path");
        }
    }

    @Override
    public T path(String path) {
        this.path = path;
        return (T) this;
    }

    @Override
    public T s3AddCredentials() {
        s3Params.setAddCredentials(true);
        return (T) this;
    }

    @Override
    public T s3AccessKeyId(String accessKeyId) {
        s3Params.setAccessKeyId(accessKeyId);
        return (T)this;
    }

    @Override
    public T s3SecretAccessKey(String secretAccessKey) {
        s3Params.setSecretAccessKey(secretAccessKey);
        return (T)this;
    }

    @Override
    public Map<String, String> get() {
        return Map.of();
    }

    @Override
    public T s3Endpoint(String endpoint) {
        s3Params.setEndpoint(endpoint);
        return (T) this;
    }

    @Override
    public T fileCount(int fileCount) {
        this.fileCount = fileCount;
        return (T) this;
    }
}
