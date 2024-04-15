package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines common parameters for any import command that reads from files.
 */
public class ReadFilesParams {

    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @Parameter(names = "--abortOnReadFailure",
        description = "Causes the command to abort when it fails to read a file."
    )
    private Boolean abortOnReadFailure = false;

    @Parameter(names = "--filter", description = "A glob filter for selecting only files with file names matching the pattern.")
    private String filter;

    @Parameter(names = "--recursiveFileLookup", arity = 1, description = "If true, files will be loaded recursively from child directories and partition inferring is disabled.")
    private Boolean recursiveFileLookup = true;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (abortOnReadFailure != null && abortOnReadFailure) {
            options.put(Options.READ_FILES_ABORT_ON_FAILURE, "true");
        } else {
            options.put(Options.READ_FILES_ABORT_ON_FAILURE, "false");
        }
        if (filter != null) {
            options.put("pathGlobFilter", filter);
        }
        if (recursiveFileLookup != null) {
            options.put("recursiveFileLookup", recursiveFileLookup.toString());
        }
        return options;
    }

    public List<String> getPaths() {
        return paths;
    }

    public S3Params getS3Params() {
        return s3Params;
    }
}
