/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For commands that export documents and must therefore read "document rows" first.
 */
public class ReadDocumentParams<T extends ReadDocumentsOptions> implements ReadDocumentsOptions<T> {

    @SuppressWarnings("java:S2386") // Sonar mistakenly thinks this can be protected.
    public static final String[] REQUIRED_QUERY_OPTIONS = new String[]{
        "--collections", "--directory", "--query", "--string-query", "--uris"
    };

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

    @CommandLine.Option(names = "--partitions-per-forest", description = "Number of partition readers to create for each forest.")
    private int partitionsPerForest = 4;

    @CommandLine.Option(
        names = "--log-progress",
        description = "Log a count of total documents read every time this many documents are read."
    )
    private int progressInterval = 10000;

    public void verifyAtLeastOneQueryOptionIsSet(String verbForErrorMessage) {
        if (makeQueryOptions().isEmpty()) {
            throw new FluxException(String.format("Must specify at least one of the following for the documents to %s: " +
                "collections; a directory; a string query; a structured, serialized, or combined query; or URIs.", verbForErrorMessage));
        }
    }

    public Map<String, String> makeOptions() {
        return OptionsUtil.addOptions(makeQueryOptions(),
            Options.READ_DOCUMENTS_OPTIONS, options,
            Options.READ_DOCUMENTS_TRANSFORM, transform,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS, transformParams,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.READ_BATCH_SIZE, OptionsUtil.intOption(batchSize),
            Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST, OptionsUtil.intOption(partitionsPerForest),
            Options.READ_LOG_PROGRESS, OptionsUtil.intOption(progressInterval)
        );
    }

    private Map<String, String> makeQueryOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_DOCUMENTS_STRING_QUERY, stringQuery,
            Options.READ_DOCUMENTS_URIS, uris,
            Options.READ_DOCUMENTS_QUERY, query,
            Options.READ_DOCUMENTS_COLLECTIONS, collections,
            Options.READ_DOCUMENTS_DIRECTORY, directory
        );
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
}
