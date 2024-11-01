/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.EmbedderOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class EmbedderParams implements EmbedderOptions {

    @CommandLine.Option(
        names = "--embedder",
        description = "The class name or abbreviation for a class that returns an instance of a langchain4j EmbeddingModel."
    )
    private String embedder;

    @CommandLine.Option(
        names = "--embedder-chunks-json-pointer",
        description = "A JSON Pointer expression that identifies the location of chunks in each document. If not " +
            "specified, defaults to '/chunks'."
    )
    private String chunksJsonPointer;

    @CommandLine.Option(
        names = "--embedder-text-json-pointer",
        description = "A JSON Pointer expression that identifies the location of the text in each chunk that is used " +
            "to generate an embedding. If not specified, defaults to '/text'."
    )
    private String textJsonPointer;

    @CommandLine.Option(
        names = "--embedder-embedding-name",
        description = "The name of the JSON array or XML element to add to a chunk that contains the generated " +
            "embedding. If not specified, defaults to 'embedding'."
    )
    private String embeddingName;

    @CommandLine.Option(
        names = {"-E"},
        description = "Specify zero to many options to pass to the class specified by the '--embedder' option - e.g. -Eapi-key=abc123 ."
    )
    private Map<String, String> embedderOptions = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (embedder != null) {
            OptionsUtil.addOptions(options,
                Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, determineClassName(embedder),
                Options.WRITE_EMBEDDER_TEXT_JSON_POINTER, textJsonPointer,
                Options.WRITE_EMBEDDER_EMBEDDING_NAME, embeddingName
            );

            // "" is a valid value, so we don't use the OptionsUtil class which ignores "".
            if (chunksJsonPointer != null) {
                options.put(Options.WRITE_EMBEDDER_CHUNKS_JSON_POINTER, chunksJsonPointer);
            }
            
            embedderOptions.entrySet().forEach(entry ->
                options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_OPTION_PREFIX + entry.getKey(), entry.getValue()));
        }
        return options;
    }

    @Override
    public EmbedderOptions embedder(String name) {
        this.embedder = name;
        return this;
    }

    private String determineClassName(String embedderValue) {
        if ("minilm".equalsIgnoreCase(embedderValue)) {
            return "com.marklogic.flux.langchain4j.embedding.MinilmEmbeddingModelFunction";
        }
        if ("azure".equalsIgnoreCase(embedderValue)) {
            return "com.marklogic.flux.langchain4j.embedding.AzureOpenAiEmbeddingModelFunction";
        }
        return embedderValue;
    }
}
