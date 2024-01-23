package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.Map;

public class WriteDocumentParams {

    // See https://jcommander.org/#_boolean .
    @Parameter(names = "--abortOnFailure", arity = 1)
    private boolean abortOnFailure = true;

    @Parameter(names = "--batchSize")
    private Integer batchSize = 100;

    @Parameter(names = "--collections", description = "Comma-delimited")
    private String collections;

    @Parameter(names = "--permissions")
    private String permissions;

    @Parameter(names = "--temporalCollection")
    private String temporalCollection;

    @Parameter(names = "--threadCount")
    private Integer threadCount = 4;

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
        return OptionsUtil.makeOptions(
            Options.WRITE_ABORT_ON_FAILURE, Boolean.toString(abortOnFailure),
            Options.WRITE_BATCH_SIZE, batchSize != null ? batchSize.toString() : null,
            Options.WRITE_COLLECTIONS, collections,
            Options.WRITE_PERMISSIONS, permissions,
            Options.WRITE_TEMPORAL_COLLECTION, temporalCollection,
            Options.WRITE_THREAD_COUNT, threadCount != null ? threadCount.toString() : null,
            Options.WRITE_TRANSFORM_NAME, transform,
            Options.WRITE_TRANSFORM_PARAMS, transformParams,
            Options.WRITE_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.WRITE_URI_PREFIX, uriPrefix,
            Options.WRITE_URI_REPLACE, uriReplace,
            Options.WRITE_URI_SUFFIX, uriSuffix,
            Options.WRITE_URI_TEMPLATE, uriTemplate
        );
    }
}
