/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.AzureStorageOptions;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.impl.AzureStorageParams;
import com.marklogic.flux.impl.CloudStorageParams;
import com.marklogic.flux.impl.S3Params;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class WriteFilesParams<T extends WriteFilesOptions> implements Supplier<Map<String, String>>, WriteFilesOptions<T>, CloudStorageParams {

    @CommandLine.Option(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @CommandLine.Mixin
    private S3Params s3Params = new S3Params();

    @CommandLine.Mixin
    private AzureStorageParams azureStorageParams = new AzureStorageParams();

    @CommandLine.Option(names = "--file-count", description = "Specifies how many files should be written; also an alias for '--repartition'.")
    protected int fileCount;

    @CommandLine.Option(
        names = {"--write-prop"},
        hidden = true,
        description = "Specify one or more arbitrary options to pass to the MarkLogic connector writer."
    )
    private Map<String, String> additionalWriteOptions = new HashMap<>();

    public String getPath() {
        return path;
    }

    @Override
    public S3Params getS3Params() {
        return s3Params;
    }

    @Override
    public AzureStorageParams getAzureStorageParams() {
        return azureStorageParams;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void validatePath() {
        if (path == null || path.trim().isEmpty()) {
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
    public T s3UseProfile() {
        s3Params.setUseProfile(true);
        return (T) this;
    }

    @Override
    public T s3AnonymousAccess() {
        s3Params.setAnonymousAccess(true);
        return (T) this;
    }

    @Override
    public T s3AccessKeyId(String accessKeyId) {
        s3Params.setAccessKeyId(accessKeyId);
        return (T) this;
    }

    @Override
    public T s3SecretAccessKey(String secretAccessKey) {
        s3Params.setSecretAccessKey(secretAccessKey);
        return (T) this;
    }

    @Override
    public final Map<String, String> get() {
        Map<String, String> options = new HashMap<>();
        addWriteOptions(options);
        if (additionalWriteOptions != null) {
            options.putAll(additionalWriteOptions);
        }
        return options;
    }

    /**
     * Allows subclasses to provide additional write options.
     *
     * @param options
     */
    protected void addWriteOptions(Map<String, String> options) {

    }

    @Override
    public T s3Endpoint(String endpoint) {
        s3Params.setEndpoint(endpoint);
        return (T) this;
    }

    @Override
    public T s3Region(String region) {
        s3Params.setRegion(region);
        return (T) this;
    }

    @Override
    public T fileCount(int fileCount) {
        this.fileCount = fileCount;
        return (T) this;
    }

    @Override
    public T azureStorage(Consumer<AzureStorageOptions> consumer) {
        consumer.accept(azureStorageParams);
        return (T) this;
    }
}
