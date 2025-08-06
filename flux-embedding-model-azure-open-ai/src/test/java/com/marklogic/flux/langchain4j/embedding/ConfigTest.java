/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigTest {

    @Test
    void minimalConfig() {
        EmbeddingModel model = apply(
            "api-key", "abc123",
            "endpoint", "https://gpt-testing-custom-data1.openai.azure.com"
        );
        assertEquals(1536, model.dimension(), "Expecting Azure OpenAI to default to 1536");
    }

    @Test
    void mostConfig() {
        EmbeddingModel model = apply(
            "api-key", "abc123",
            "endpoint", "https://gpt-testing-custom-data1.openai.azure.com",
            "deployment-name", "anything",
            "dimensions", "384",
            "max-retries", "2",
            "log-requests-and-responses", "true",
            "duration", "10"
        );

        assertEquals(384, model.dimension());
    }

    @Test
    void missingEndpoint() {
        expectError(
            "endpoint cannot be null or blank",
            "api-key", "abc123"
        );
    }

    @Test
    void endpointAndNoApiKey() {
        expectError(
            "Must specify either api-key or non-azure-api-key.",
            "endpoint", "https://gpt-testing-custom-data1.openai.azure.com"
        );
    }

    @Test
    void badDimensions() {
        expectError(
            "dimensions must have a numeric value; invalid value: abc",
            "api-key", "abc123",
            "dimensions", "abc"
        );
    }

    @Test
    void badMaxRetries() {
        expectError(
            "max-retries must have a numeric value; invalid value: abc",
            "max-retries", "abc",
            "api-key", "abc123"
        );
    }

    @Test
    void badDuration() {
        expectError(
            "duration must have a numeric value; invalid value: abc",
            "duration", "abc",
            "api-key", "abc123"
        );
    }

    @Test
    void nonAzureApiKey() {
        EmbeddingModel model = apply(
            "non-azure-api-key", "anything"
        );
        assertEquals(1536, model.dimension());
    }

    private EmbeddingModel apply(String... keysAndValues) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            options.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return new AzureOpenAiEmbeddingModelFunction().apply(options);
    }

    private void expectError(String message, String... keysAndValues) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> apply(keysAndValues));
        assertEquals(message, ex.getMessage());
    }
}
