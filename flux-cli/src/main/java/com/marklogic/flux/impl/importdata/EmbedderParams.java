/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
        names = "--embedder-prompt",
        description = "A prompt to prepend to the text of each chunk before generating an embedding."
    )
    private String prompt;

    @CommandLine.Option(
        names = {"-E"},
        description = "Specify zero to many options to pass to the class specified by the '--embedder' option - e.g. -Eapi-key=abc123 ."
    )
    private Map<String, String> embeddingModelOptions = new HashMap<>();

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
        names = "--embedder-chunks-xpath",
        description = "An XPath expression that identifies the location of chunks in each document. If not " +
            "specified, defaults to '/node()/chunks'."
    )
    private String chunksXpath;

    @CommandLine.Option(
        names = "--embedder-text-xpath",
        description = "An XPath expression that identifies the location of the text in each chunk that is used " +
            "to generate an embedding. If not specified, defaults to 'text'."
    )
    private String textXpath;

    @CommandLine.Option(
        names = "--embedder-embedding-name",
        description = "The name of the JSON array or XML element to add to a chunk that contains the generated " +
            "embedding. If not specified, defaults to 'embedding'."
    )
    private String embeddingName;

    @CommandLine.Option(
        names = "--embedder-embedding-namespace",
        description = "Optional namespace to assign to the embedding element added to XML chunks."
    )
    private String embeddingNamespace;

    @CommandLine.Option(
        names = "--embedder-batch-size",
        description = "Number of chunks to send in a single call to the embedding model."
    )
    private Integer batchSize;

    @CommandLine.Option(
        names = "--embedder-base64-encode",
        description = "If specified, the embedding vector will be base64-encoded and stored as a single string value " +
            "in the document. If not specified, the embedding vector will be stored as an array of numbers."
    )
    private boolean base64Encode;

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (embedder != null) {
            OptionsUtil.addOptions(options,
                Options.WRITE_EMBEDDER_PROMPT, prompt,
                Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, determineClassName(embedder),
                Options.WRITE_EMBEDDER_TEXT_JSON_POINTER, textJsonPointer,
                Options.WRITE_EMBEDDER_EMBEDDING_NAME, embeddingName,
                Options.WRITE_EMBEDDER_CHUNKS_XPATH, chunksXpath,
                Options.WRITE_EMBEDDER_TEXT_XPATH, textXpath,
                Options.WRITE_EMBEDDER_BATCH_SIZE, OptionsUtil.integerOption(batchSize),
                Options.WRITE_EMBEDDER_BASE64_ENCODE, base64Encode ? "true" : null
            );

            // Empty string is a valid value.
            if (embeddingNamespace != null) {
                options.put(Options.WRITE_EMBEDDER_EMBEDDING_NAMESPACE, embeddingNamespace);
            }

            // "" is a valid value, so we don't use the OptionsUtil class which ignores "".
            if (chunksJsonPointer != null) {
                options.put(Options.WRITE_EMBEDDER_CHUNKS_JSON_POINTER, chunksJsonPointer);
            }

            embeddingModelOptions.entrySet().forEach(entry ->
                options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_OPTION_PREFIX + entry.getKey(), entry.getValue()));
        }
        return options;
    }

    @Override
    public EmbedderOptions embedder(String name) {
        this.embedder = name;
        return this;
    }

    @Override
    public EmbedderOptions chunksJsonPointer(String jsonPointer) {
        this.chunksJsonPointer = jsonPointer;
        return this;
    }

    @Override
    public EmbedderOptions textJsonPointer(String jsonPointer) {
        this.textJsonPointer = jsonPointer;
        return this;
    }

    @Override
    public EmbedderOptions chunksXPath(String xpath) {
        this.chunksXpath = xpath;
        return this;
    }

    @Override
    public EmbedderOptions textXPath(String xpath) {
        this.textXpath = xpath;
        return this;
    }

    @Override
    public EmbedderOptions embeddingName(String embeddingName) {
        this.embeddingName = embeddingName;
        return this;
    }

    @Override
    public EmbedderOptions embeddingNamespace(String embeddingNamespace) {
        this.embeddingNamespace = embeddingNamespace;
        return this;
    }

    @Override
    public EmbedderOptions batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public EmbedderOptions embedderOptions(Map<String, String> options) {
        this.embeddingModelOptions = options;
        return this;
    }

    @Override
    public EmbedderOptions prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    @Override
    public EmbedderOptions base64Encode() {
        this.base64Encode = true;
        return this;
    }

    private String determineClassName(final String embedderValue) {
        String abbreviation = embedderValue.toLowerCase();
        switch (abbreviation) {
            case "minilm":
                return "com.marklogic.flux.langchain4j.embedding.MinilmEmbeddingModelFunction";
            case "azure":
                return "com.marklogic.flux.langchain4j.embedding.AzureOpenAiEmbeddingModelFunction";
            case "ollama":
                return "com.marklogic.flux.langchain4j.embedding.OllamaEmbeddingModelFunction";
            default:
                return embedderValue;
        }
    }
}
