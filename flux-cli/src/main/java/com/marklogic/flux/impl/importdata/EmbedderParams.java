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

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (embedder != null) {
            if ("minilm".equals(embedder)) {
                options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME,
                    "com.marklogic.flux.langchain4j.embedding.MinilmEmbeddingModelFunction");
            } else {
                options.put(Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, embedder);
            }
        }
        return options;
    }

    @Override
    public EmbedderOptions embedder(String name) {
        this.embedder = name;
        return this;
    }
}
