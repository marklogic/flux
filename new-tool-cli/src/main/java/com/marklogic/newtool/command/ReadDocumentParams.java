package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * For commands that export documents and must therefore read "document rows" first.
 */
public class ReadDocumentParams {

    @Parameter(names = "--stringQuery", description = "A query utilizing the MarkLogic search grammar; " +
        "see https://docs.marklogic.com/guide/search-dev/string-query for more information.")
    private String stringQuery;

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

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_DOCUMENTS_STRING_QUERY, stringQuery,
            Options.READ_DOCUMENTS_QUERY, query,
            Options.READ_DOCUMENTS_OPTIONS, options,
            Options.READ_DOCUMENTS_COLLECTIONS, collections,
            Options.READ_DOCUMENTS_DIRECTORY, directory,
            Options.READ_DOCUMENTS_TRANSFORM, transform,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS, transformParams,
            Options.READ_DOCUMENTS_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.READ_BATCH_SIZE, batchSize != null ? batchSize.toString() : null
        );
    }
}
