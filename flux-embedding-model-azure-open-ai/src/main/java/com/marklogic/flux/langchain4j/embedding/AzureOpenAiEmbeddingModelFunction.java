/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class AzureOpenAiEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        // See https://docs.langchain4j.dev/integrations/embedding-models/azure-open-ai/#spring-boot-1 for reference
        // of all properties that should be configurable.
        final String nonAzureKey = "non-azure-api-key";
        if (!options.containsKey("api-key") && !options.containsKey(nonAzureKey)) {
            throw new IllegalArgumentException(String.format("Must specify either api-key or %s.", nonAzureKey));
        }

        AzureOpenAiEmbeddingModel.Builder builder = AzureOpenAiEmbeddingModel.builder()
            .apiKey(options.get("api-key"))
            .deploymentName(options.get("deployment-name"))
            .endpoint(options.get("endpoint"))
            .dimensions(getInteger(options, "dimensions"))
            .maxRetries(getInteger(options, "max-retries"));

        if (options.containsKey(nonAzureKey)) {
            builder.nonAzureApiKey(options.get(nonAzureKey));
        }

        if (options.containsKey("log-requests-and-responses")) {
            builder.logRequestsAndResponses(Boolean.parseBoolean(options.get("log-requests-and-responses")));
        }

        if (options.containsKey("duration")) {
            Integer duration = getInteger(options, "duration");
            if (duration != null) {
                builder.timeout(Duration.ofSeconds(duration));
            }
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
