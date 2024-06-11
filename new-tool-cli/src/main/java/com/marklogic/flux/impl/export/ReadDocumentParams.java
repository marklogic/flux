package com.marklogic.flux.impl.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.AtLeastOneValidator;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For commands that export documents and must therefore read "document rows" first.
 */
@Parameters(parametersValidators = ReadDocumentParams.Validator.class)
public class ReadDocumentParams<T extends ReadDocumentsOptions> implements ReadDocumentsOptions<T> {

    public static class Validator extends AtLeastOneValidator {
        public Validator() {
            super("--query", "--uris", "--stringQuery", "--collections", "--directory");
        }
    }

    @Parameter(names = "--stringQuery", description = "A query utilizing the MarkLogic search grammar; " +
        "see https://docs.marklogic.com/guide/search-dev/string-query for more information.")
    private String stringQuery;

    @Parameter(names = "--uris", description = "Newline-delimited sequence of document URIs to retrieve. Can be combined " +
        "with --collections, --directory, and --stringQuery. If specified, --query will be ignored.")
    private String uris;

    @Parameter(names = "--query", description = "A JSON or XML representation of a structured query, serialized CTS query, or combined query. " +
        "See https://docs.marklogic.com/guide/rest-dev/search#id_49329 for more information.")
    private String query;

    @Parameter(names = "--options", description = "Name of a set of MarkLogic REST API search options.")
    private String options;

    @Parameter(names = "--collections", description = "Comma-delimited sequence of collection names by which to constrain the query.")
    private String collections;

    @Parameter(names = "--directory", description = "Database directory by which to constrain the query.")
    private String directory;

    @Parameter(names = "--transform", description = "Name of a MarkLogic REST API transform to apply to each matching document.")
    private String transform;

    @Parameter(names = "--transformParams", description = "Comma-delimited sequence of transform parameter names and values - e.g. param1,value1,param2,value2.")
    private String transformParams;

    @Parameter(names = "--transformParamsDelimiter", description = "Delimiter for transform parameters; defaults to a comma.")
    private String transformParamsDelimiter;

    @Parameter(names = "--batchSize", description = "Number of documents to retrieve in each call to MarkLogic.")
    private Integer batchSize = 500;

    @Parameter(names = "--partitionsPerForest", description = "Number of partition readers to create for each forest.")
    private Integer partitionsPerForest = 4;

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_DOCUMENTS_STRING_QUERY, stringQuery,
            Options.READ_DOCUMENTS_URIS, uris,
            Options.READ_DOCUMENTS_QUERY, query,
            Options.READ_DOCUMENTS_OPTIONS, options,
            Options.READ_DOCUMENTS_COLLECTIONS, collections,
            Options.READ_DOCUMENTS_DIRECTORY, directory,
            Options.READ_DOCUMENTS_TRANSFORM, transform,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS, transformParams,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.READ_BATCH_SIZE, batchSize != null ? batchSize.toString() : null,
            Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST, partitionsPerForest != null ? partitionsPerForest.toString() : null
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
    public T batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return (T) this;
    }

    @Override
    public T partitionsPerForest(Integer partitionsPerForest) {
        this.partitionsPerForest = partitionsPerForest;
        return (T) this;
    }
}
