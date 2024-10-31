/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.EmbedderOptions;
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
        names = {"-E"},
        description = "Specify zero to many options to pass to the class specified by the '--embedder' option - e.g. -Eapi-key=abc123 ."
    )
    private Map<String, String> embedderOptions = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (embedder != null) {
            options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, determineClassName(embedder));
        }
        embedderOptions.entrySet().forEach(entry ->
            options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_OPTION_PREFIX + entry.getKey(), entry.getValue()));
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
