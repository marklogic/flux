package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * Defines all basic params for writing documents. Does not include support for a URI template, as that is not always
 * relevant nor possible depending on what kind of data is being imported.
 */
public class WriteDocumentParams {

    // See https://jcommander.org/#_boolean for a description of the 'arity' field.
    @Parameter(
        names = "--abortOnFailure", arity = 1,
        description = "Set to false to cause the command to continue processing data after a write fails."
    )
    private boolean abortOnFailure = true;

    @Parameter(
        names = "--batchSize",
        description = "The number of documents written in a call to MarkLogic."
    )
    private Integer batchSize = 200;

    @Parameter(
        names = "--collections",
        description = "Comma-delimited string of collection names to add to each document."
    )
    private String collections;

    @Parameter(
        names = "--permissions",
        description = "Comma-delimited string of role names and capabilities to add to each document - e.g. role1,read,role2,update,role3,execute."
    )
    private String permissions;

    @Parameter(
        names = "--temporalCollection",
        description = "Name of a temporal collection to assign to each document."
    )
    private String temporalCollection;

    @Parameter(
        names = "--threadCount",
        description = "The number of threads used by each partition worker when writing batches of documents to MarkLogic."
    )
    private Integer threadCount = 4;

    @Parameter(
        names = "--transform",
        description = "Name of a MarkLogic REST API transform to apply to each document."
    )
    private String transform;

    @Parameter(
        names = "--transformParams",
        description = "Comma-delimited string of REST API transform parameter names and values - e.g. param1,value1,param2,value2."
    )
    private String transformParams;

    @Parameter(
        names = "--transformParamsDelimiter",
        description = "Delimiter to use instead of a comma for the '--transformParams' parameter."
    )
    private String transformParamsDelimiter;

    @Parameter(
        names = "--uriPrefix",
        description = "String to prepend to each document URI."
    )
    private String uriPrefix;

    @Parameter(
        names = "--uriReplace",
        description = "Modify the URI for a document via a comma-delimited list of regular expression " +
            "and replacement string pairs - e.g. regex,'value',regex,'value'. Each replacement string must be enclosed by single quotes."
    )
    private String uriReplace;

    @Parameter(
        names = "--uriSuffix",
        description = "String to append to each document URI."
    )
    private String uriSuffix;

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
            Options.WRITE_URI_SUFFIX, uriSuffix
        );
    }
}
