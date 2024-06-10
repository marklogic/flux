package com.marklogic.newtool.impl.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.ReadFilesOptions;
import com.marklogic.newtool.impl.S3Params;
import com.marklogic.spark.Options;

import java.util.*;

/**
 * Defines common parameters for any import command that reads from files.
 */
public class ReadFilesParams<T extends ReadFilesOptions> implements ReadFilesOptions<T> {

    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @Parameter(names = "--abort-on-read-failure", description = "Causes the command to abort when it fails to read a file.")
    private Boolean abortOnReadFailure = false;

    @Parameter(names = "--filter", description = "A glob filter for selecting only files with file names matching the pattern.")
    private String filter;

    @Parameter(names = "--recursive-file-lookup", arity = 1, description = "If true, files will be loaded recursively from child directories and partition inferring is disabled.")
    private Boolean recursiveFileLookup = true;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    public boolean hasAtLeastOnePath() {
        return paths != null && !paths.isEmpty();
    }

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (abortOnReadFailure != null && abortOnReadFailure) {
            addOptionsToFailOnReadError(options);
        } else {
            addOptionsToNotFailOnReadError(options);
        }

        if (filter != null) {
            options.put("pathGlobFilter", filter);
        }
        if (recursiveFileLookup != null) {
            options.put("recursiveFileLookup", recursiveFileLookup.toString());
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
        return paths;
    }

    public S3Params getS3Params() {
        return s3Params;
    }

    @Override
    public T paths(String... paths) {
        this.paths = Arrays.asList(paths);
        return (T) this;
    }

    @Override
    public T filter(String filter) {
        this.filter = filter;
        return (T) this;
    }

    @Override
    public T recursiveFileLookup(Boolean value) {
        this.recursiveFileLookup = value;
        return (T) this;
    }

    @Override
    public T abortOnReadFailure(Boolean value) {
        this.abortOnReadFailure = value;
        return (T) this;
    }

    @Override
    public T s3AddCredentials() {
        this.s3Params.setAddCredentials(true);
        return (T) this;
    }

    @Override
    public T s3Endpoint(String endpoint) {
        this.s3Params.setEndpoint(endpoint);
        return (T) this;
    }
}
