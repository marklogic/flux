/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For commands that export documents and must therefore read "document rows" first.
 */
public class ReadDocumentParams<T extends ReadDocumentsOptions> implements ReadDocumentsOptions<T> {

    @CommandLine.Option(names = "--string-query", description = "A query utilizing the MarkLogic search grammar; " +
        "see %nhttps://docs.marklogic.com/guide/search-dev/string-query for more information.")
    private String stringQuery;

    @CommandLine.Option(names = "--uris", description = "Newline-delimited sequence of document URIs to retrieve. Can be combined " +
        "with --collections, --directory, and --string-query. If specified, --query will be ignored.")
    private String uris;

    @CommandLine.Option(names = "--query", description = "A JSON or XML representation of a structured query, serialized CTS query, or combined query. " +
        "See https://docs.marklogic.com/guide/rest-dev/search#id_49329 for more information.")
    private String query;

    @CommandLine.Option(names = "--options", description = "Name of a set of MarkLogic REST API search options.")
    private String options;

    @CommandLine.Option(names = "--collections", description = "Comma-delimited sequence of collection names by which to constrain the query.")
    private String collections;

    @CommandLine.Option(names = "--directory", description = "Database directory by which to constrain the query.")
    private String directory;

    @CommandLine.Option(names = "--transform", description = "Name of a MarkLogic REST API transform to apply to each matching document.")
    private String transform;

    @CommandLine.Option(names = "--transform-params", description = "Comma-delimited sequence of transform parameter names and values - e.g. param1,value1,param2,value2.")
    private String transformParams;

    @CommandLine.Option(names = "--transform-params-delimiter", description = "Delimiter for transform parameters; defaults to a comma.")
    private String transformParamsDelimiter;

    @CommandLine.Option(names = "--batch-size", description = "Number of documents to retrieve in each call to MarkLogic.")
    private int batchSize = 500;

    @CommandLine.Option(names = "--secondary-uris-invoke", description = "Module path to invoke to provide additional URIs.")
    private String secondaryUrisInvoke;

    @CommandLine.Option(names = "--secondary-uris-javascript", description = "JavaScript code to provide additional URIs.")
    private String secondaryUrisJavascript;

    @CommandLine.Option(names = "--secondary-uris-xquery", description = "XQuery code to provide additional URIs.")
    private String secondaryUrisXquery;

    @CommandLine.Option(names = "--secondary-uris-javascript-file", description = "JavaScript file to provide additional URIs.")
    private String secondaryUrisJavascriptFile;

    @CommandLine.Option(names = "--secondary-uris-xquery-file", description = "XQuery file to provide additional URIs.")
    private String secondaryUrisXqueryFile;

    @CommandLine.Option(
        names = "--secondary-uris-var", arity = "*",
        description = "Define variables to be sent to the code for secondary URIs; e.g. '--secondary-uris-var var1=value1'."
    )
    private Map<String, String> secondaryUrisVars = new HashMap<>();

    @CommandLine.Option(names = "--partitions-per-forest", description = "Number of partition readers to create for each forest.")
    private int partitionsPerForest = 4;

    @CommandLine.Option(
        names = "--log-progress",
        description = "Log a count of total documents read every time this many documents are read."
    )
    private int progressInterval = 10000;

    @CommandLine.Option(
        names = "--no-snapshot",
        description = "Read data from MarkLogic at multiple points in time instead of using a consistent snapshot."
    )
    private boolean noSnapshot;

    public Map<String, String> makeOptions() {
        Map<String, String> optionsMap = OptionsUtil.addOptions(makeQueryOptions(),
            Options.READ_DOCUMENTS_OPTIONS, options,
            Options.READ_DOCUMENTS_TRANSFORM, transform,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS, transformParams,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.READ_BATCH_SIZE, OptionsUtil.intOption(batchSize),
            Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST, OptionsUtil.intOption(partitionsPerForest),
            Options.READ_LOG_PROGRESS, OptionsUtil.intOption(progressInterval),
            Options.READ_SNAPSHOT, noSnapshot ? "false" : null,
            Options.READ_SECONDARY_URIS_INVOKE, secondaryUrisInvoke,
            Options.READ_SECONDARY_URIS_JAVASCRIPT, secondaryUrisJavascript,
            Options.READ_SECONDARY_URIS_XQUERY, secondaryUrisXquery,
            Options.READ_SECONDARY_URIS_JAVASCRIPT_FILE, secondaryUrisJavascriptFile,
            Options.READ_SECONDARY_URIS_XQUERY_FILE, secondaryUrisXqueryFile);

        if (secondaryUrisVars != null) {
            secondaryUrisVars.entrySet().forEach(entry ->
                optionsMap.put(Options.READ_SECONDARY_URIS_VARS_PREFIX + entry.getKey(), entry.getValue()));
        }

        return optionsMap;
    }

    private Map<String, String> makeQueryOptions() {
        final Map<String, String> queryOptions = OptionsUtil.makeOptions(
            Options.READ_DOCUMENTS_STRING_QUERY, stringQuery,
            Options.READ_DOCUMENTS_URIS, uris,
            Options.READ_DOCUMENTS_QUERY, query,
            Options.READ_DOCUMENTS_COLLECTIONS, collections,
            Options.READ_DOCUMENTS_DIRECTORY, directory
        );

        if (queryOptions.isEmpty()) {
            String trueQuery = "{\"query\": {\"queries\": [{\"true-query\": null}]}}";
            queryOptions.put(Options.READ_DOCUMENTS_QUERY, trueQuery);
        }
        return queryOptions;
    }

    @Override
    public T stringQuery(String stringQuery) {
        this.stringQuery = stringQuery;
        return (T) this;
    }

    @Override
    public T uris(String... uris) {
        this.uris = Stream.of(uris).collect(Collectors.joining(","));
        return (T) this;
    }

    @Override
    public T query(String query) {
        this.query = query;
        return (T) this;
    }

    @Override
    public T options(String options) {
        this.options = options;
        return (T) this;
    }

    @Override
    public T collections(String... collections) {
        this.collections = Stream.of(collections).collect(Collectors.joining(","));
        return (T) this;
    }

    @Override
    public T directory(String directory) {
        this.directory = directory;
        return (T) this;
    }

    @Override
    public T logProgress(int interval) {
        this.progressInterval = interval;
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
    public T batchSize(int batchSize) {
        this.batchSize = batchSize;
        return (T) this;
    }

    @Override
    public T partitionsPerForest(int partitionsPerForest) {
        this.partitionsPerForest = partitionsPerForest;
        return (T) this;
    }

    @Override
    public T noSnapshot() {
        this.noSnapshot = true;
        return (T) this;
    }

    @Override
    public T secondaryUrisJavaScript(String secondaryUrisJavaScript) {
        this.secondaryUrisJavascript = secondaryUrisJavaScript;
        return (T) this;
    }

    @Override
    public T secondaryUrisXQuery(String secondaryUrisXQuery) {
        this.secondaryUrisXquery = secondaryUrisXQuery;
        return (T) this;
    }

    @Override
    public T secondaryUrisJavaScriptFile(String secondaryUrisJavaScriptFile) {
        this.secondaryUrisJavascriptFile = secondaryUrisJavaScriptFile;
        return (T) this;
    }

    @Override
    public T secondaryUrisXQueryFile(String secondaryUrisXQueryFile) {
        this.secondaryUrisXqueryFile = secondaryUrisXQueryFile;
        return (T) this;
    }

    @Override
    public T secondaryUrisInvoke(String secondaryUrisInvoke) {
        this.secondaryUrisInvoke = secondaryUrisInvoke;
        return (T) this;
    }

    @Override
    public T secondaryUrisVars(Map<String, String> secondaryUrisVars) {
        this.secondaryUrisVars = secondaryUrisVars;
        return (T) this;
    }
}
