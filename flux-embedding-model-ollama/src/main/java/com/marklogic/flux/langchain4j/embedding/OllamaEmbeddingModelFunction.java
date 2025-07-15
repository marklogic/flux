/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class OllamaEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        if (!options.containsKey("base-url")) {
            throw new IllegalArgumentException("Must specify -Ebase-url= for connecting to Ollama.");
        }
        if (!options.containsKey("model-name")) {
            throw new IllegalArgumentException("Must specify -Emodel-name= for connecting to Ollama.");
        }

        OllamaEmbeddingModel.OllamaEmbeddingModelBuilder builder = OllamaEmbeddingModel.builder()
            .baseUrl(options.get("base-url"))
            .modelName(options.get("model-name"))
            .maxRetries(getInteger(options, "max-retries"));

        if (options.containsKey("duration")) {
            Integer duration = getInteger(options, "duration");
            if (duration != null) {
                builder.timeout(Duration.ofSeconds(duration));
            }
        }
        if (options.containsKey("log-requests")) {
            builder.logRequests(Boolean.valueOf(options.get("log-requests")));
        }
        if (options.containsKey("log-responses")) {
            builder.logResponses(Boolean.valueOf(options.get("log-responses")));
        }

        return builder.build();
    }

    private Integer getInteger(Map<String, String> options, String key) {
        if (options.containsKey(key)) {
            try {
                return Integer.parseInt(options.get(key));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("%s must have a numeric value; invalid value: %s",
                    key, options.get(key)));
            }
        }
        return null;
    }
}
