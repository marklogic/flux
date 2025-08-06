/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.AzureStorageOptions;
import com.marklogic.flux.api.ReadFilesOptions;
import com.marklogic.flux.impl.AzureStorageParams;
import com.marklogic.flux.impl.S3Params;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.*;
import java.util.function.Consumer;

/**
 * Defines common parameters for any import command that reads from files.
 */
@SuppressWarnings("unchecked")
public class ReadFilesParams<T extends ReadFilesOptions> implements ReadFilesOptions<T>, IReadFilesParams {

    // "path" is the name so that picocli shows "--path <path>" instead of "--path <paths>".
    @CommandLine.Option(
        required = true,
        names = "--path",
        description = "One or more path expressions for selecting files to import.",
        // For a path like "/opt/data*.xml", the user's shell may resolve the asterisk and determine a list of all the
        // files that match. Each file then becomes a value passed to this List. In order for that to work properly,
        // we need arity set to allow for unlimited values. See https://picocli.info/#_arity for more information.
        arity = "1..*"
    )
    private List<String> path = new ArrayList<>();

    @CommandLine.Option(names = "--abort-on-read-failure", description = "Causes the command to abort when it fails to read a file.")
    private boolean abortOnReadFailure;

    @CommandLine.Option(names = "--filter", description = "A glob filter for selecting only files with file names matching the pattern.")
    private String filter;

    @CommandLine.Option(names = "--recursive-file-lookup", arity = "1", description = "If true, files will be loaded recursively from child directories and partition inferring is disabled.")
    private boolean recursiveFileLookup = true;

    @CommandLine.Mixin
    private S3Params s3Params = new S3Params();

    @CommandLine.Mixin
    private AzureStorageParams azureStorageParams = new AzureStorageParams();

    private boolean streaming;

    @Override
    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (abortOnReadFailure) {
            addOptionsToFailOnReadError(options);
        } else {
            addOptionsToNotFailOnReadError(options);
        }

        if (filter != null) {
            options.put("pathGlobFilter", filter);
        }
        if (recursiveFileLookup) {
            options.put("recursiveFileLookup", "true");
        }

        if (streaming) {
            options.put(Options.STREAM_FILES, "true");
        }

        return options;
    }

    /**
     * For the Spark data sources, we don't get the ideal user experience here, as we haven't figured out if it's
     * possible to configure Spark to log something about failed files/rows, which we're able to do with our own
     * connector. So the Spark reader will skip over invalid data without any logging for the user to see. This will
     * likely be fine for a scenario such as trying to read Avro files with a Parquet reader, where the user shouldn't
     * have an expectation that the Avro files are processed correctly.
     *
     * @param options
     */
    private void addOptionsToNotFailOnReadError(Map<String, String> options) {
        // For commands that use the MarkLogic connector to read files.
        options.put(Options.READ_FILES_ABORT_ON_FAILURE, "false");
        // For commands that use Spark JSON / CSV.
        options.put("mode", "DROPMALFORMED");
        // For commands that use Spark Parquet / Avro / ORC. This does not appear to affect Spark JSON / CSV, as
        // those use the 'mode' option to determine what to do with corrupt data.
        options.put("ignoreCorruptFiles", "true");
    }

    private void addOptionsToFailOnReadError(Map<String, String> options) {
        // For commands that use the MarkLogic connector to read files.
        options.put(Options.READ_FILES_ABORT_ON_FAILURE, "true");
        // For commands that use Spark JSON / CSV to read files.
        options.put("mode", "FAILFAST");
        // For commands that use Spark Parquet / Avro / ORC.
        options.put("ignoreCorruptFiles", "false");
    }

    public List<String> getPaths() {
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

    @Override
    public T paths(String... paths) {
        this.path = Arrays.asList(paths);
        return (T) this;
    }

    @Override
    public T filter(String filter) {
        this.filter = filter;
        return (T) this;
    }

    @Override
    public T recursiveFileLookup(boolean value) {
        this.recursiveFileLookup = value;
        return (T) this;
    }

    @Override
    public T abortOnReadFailure(boolean value) {
        this.abortOnReadFailure = value;
        return (T) this;
    }

    @Override
    public T s3AddCredentials() {
        this.s3Params.setAddCredentials(true);
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
    public T s3Endpoint(String endpoint) {
        this.s3Params.setEndpoint(endpoint);
        return (T) this;
    }

    @Override
    public T azureStorage(Consumer<AzureStorageOptions> consumer) {
        consumer.accept(this.azureStorageParams);
        return (T) this;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }
}
