package com.marklogic.newtool.impl.importdata;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.api.WriteDocumentsOptions;
import com.marklogic.newtool.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines all basic params for writing documents. Does not include support for a URI template, as that is not always
 * relevant nor possible depending on what kind of data is being imported.
 */
public class WriteDocumentParams<T extends WriteDocumentsOptions> implements WriteDocumentsOptions<T>, Supplier<Map<String, String>> {

    @Parameter(
        names = "--abortOnWriteFailure",
        description = "Include this option to cause the command to fail when a batch of documents cannot be written to MarkLogic."
    )
    private Boolean abortOnWriteFailure;

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
        names = "--failedDocumentsPath",
        description = "File path for writing an archive file containing failed documents and their metadata."
    )
    private String failedDocumentsPath;

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

    @Parameter(
        names = "--uriTemplate",
        description = "String defining a template for constructing each document URI. " +
            "See https://marklogic.github.io/marklogic-spark-connector/writing.html for more information."
    )
    private String uriTemplate;

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_ABORT_ON_FAILURE, abortOnWriteFailure != null ? Boolean.toString(abortOnWriteFailure) : "false",
            Options.WRITE_ARCHIVE_PATH_FOR_FAILED_DOCUMENTS, failedDocumentsPath,
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

    @Override
    public Map<String, String> get() {
        return makeOptions();
    }

    @Override
    public T abortOnWriteFailure(Boolean value) {
        this.abortOnWriteFailure = value;
        return (T) this;
    }

    @Override
    public T batchSize(int batchSize) {
        this.batchSize = batchSize;
        return (T) this;
    }

    @Override
    public T collections(String... collections) {
        this.collections = Stream.of(collections).collect(Collectors.joining(","));
        return (T) this;
    }

    @Override
    public T collectionsString(String commaDelimitedCollections) {
        this.collections = commaDelimitedCollections;
        return (T) this;
    }

    @Override
    public T failedDocumentsPath(String path) {
        this.failedDocumentsPath = path;
        return (T) this;
    }

    @Override
    public T permissionsString(String rolesAndCapabilities) {
        this.permissions = rolesAndCapabilities;
        return (T) this;
    }

    @Override
    public T temporalCollection(String temporalCollection) {
        this.temporalCollection = temporalCollection;
        return (T) this;
    }

    @Override
    public T threadCount(int threadCount) {
        this.threadCount = threadCount;
        return (T) this;
    }

    @Override
    public T transform(String transform) {
        this.transform = transform;
        return (T) this;
    }

    @Override
    public T transformParams(String delimitedNamesAndValues) {
        this.transformParams = delimitedNamesAndValues;
        return (T) this;
    }

    @Override
    public T transformParamsDelimiter(String delimiter) {
        this.transformParamsDelimiter = delimiter;
        return (T) this;
    }

    @Override
    public T uriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
        return (T) this;
    }

    @Override
    public T uriReplace(String uriReplace) {
        this.uriReplace = uriReplace;
        return (T) this;
    }

    @Override
    public T uriSuffix(String uriSuffix) {
        this.uriSuffix = uriSuffix;
        return (T) this;
    }

    @Override
    public T uriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
        return (T) this;
    }
}
