/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaConfigTest {

    @Test
    void minimalConfig() {
        EmbeddingModel model = apply(
            "base-url", "http://localhost:8008",
            "model-name", "all-minilm"
        );
        assertEquals(384, model.dimension(), "The all-minilm model defaults to 384 dimensions.");
    }

    @Test
    void allConfigOptions() {
        EmbeddingModel model = apply(
            "base-url", "http://localhost:8008",
            "model-name", "all-minilm",
            "log-requests", "true",
            "log-responses", "true",
            "max-retries", "2",
            "duration", "10"
        );

        assertEquals(384, model.dimension());
    }

    @Test
    void noBaseUrl() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            apply("model-name", "all-minilm"));
        assertEquals("Must specify -Ebase-url= for connecting to Ollama.", ex.getMessage());
    }

    @Test
    void noModelName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            apply("base-url", "http://localhost:8008"));
        assertEquals("Must specify -Emodel-name= for connecting to Ollama.", ex.getMessage());
    }

    private EmbeddingModel apply(String... keysAndValues) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            options.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return new OllamaEmbeddingModelFunction().apply(options);
    }
}
