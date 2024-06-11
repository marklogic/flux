package com.marklogic.flux.impl.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.impl.S3Params;

import java.util.Map;
import java.util.function.Supplier;

public abstract class WriteFilesParams<T extends WriteFilesOptions> implements Supplier<Map<String, String>>, WriteFilesOptions<T> {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--file-count", description = "Specifies how many files should be written; also an alias for '--repartition'.")
    protected Integer fileCount;

    public String getPath() {
        return path;
    }

    public S3Params getS3Params() {
        return s3Params;
    }

    public Integer getFileCount() {
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
    public T s3Endpoint(String endpoint) {
        s3Params.setEndpoint(endpoint);
        return (T) this;
    }

    @Override
    public T fileCount(Integer fileCount) {
        this.fileCount = fileCount;
        return (T) this;
    }
}
