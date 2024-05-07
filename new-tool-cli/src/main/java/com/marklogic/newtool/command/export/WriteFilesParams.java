package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.S3Params;

import java.util.Map;
import java.util.function.Supplier;

public abstract class WriteFilesParams implements Supplier<Map<String, String>> {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--fileCount", description = "Specifies how many files should be written; also an alias for '--repartition'.")
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
}
