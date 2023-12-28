package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

public class WriteDocumentParams {

    // See https://jcommander.org/#_boolean .
    @Parameter(names = "--abortOnFailure", arity = 1)
    private boolean abortOnFailure = true;

    @Parameter(names = "--batchSize")
    private int batchSize = 100;

    @Parameter(names = "--collections", description = "Comma-delimited")
    private String collections;

    @Parameter(names = "--permissions")
    private String permissions = "new-tool-role,read,new-tool-role,update";

    @Parameter(names = "--temporalCollection")
    private String temporalCollection;

    @Parameter(names = "--threadCount")
    private int threadCount = 4;

    @Parameter(names = "--transform")
    private String transform;

    @Parameter(names = "--transformParams")
    private String transformParams;

    @Parameter(names = "--transformParamsDelimiter")
    private String transformParamsDelimiter;

    @Parameter(names = "--uriPrefix")
    private String uriPrefix;

    @Parameter(names = "--uriReplace")
    private String uriReplace;

    @Parameter(names = "--uriSuffix")
    private String uriSuffix;

    @Parameter(names = "--uriTemplate")
    private String uriTemplate;

    public Map<String, String> makeOptions() {
        Map<String, String> map = new HashMap<>();
        map.put(Options.WRITE_ABORT_ON_FAILURE, Boolean.toString(abortOnFailure));
        map.put(Options.WRITE_BATCH_SIZE, Integer.toString(batchSize));
        map.put(Options.WRITE_COLLECTIONS, collections);
        map.put(Options.WRITE_PERMISSIONS, permissions);
        map.put(Options.WRITE_TEMPORAL_COLLECTION, temporalCollection);
        map.put(Options.WRITE_THREAD_COUNT, Integer.toString(threadCount));
        map.put(Options.WRITE_TRANSFORM_NAME, transform);
        map.put(Options.WRITE_TRANSFORM_PARAMS, transformParams);
        map.put(Options.WRITE_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter);
        map.put(Options.WRITE_URI_PREFIX, uriPrefix);
        map.put(Options.WRITE_URI_REPLACE, uriReplace);
        map.put(Options.WRITE_URI_SUFFIX, uriSuffix);
        map.put(Options.WRITE_URI_TEMPLATE, uriTemplate);
        return map;
    }
}
