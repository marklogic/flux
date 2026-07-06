/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class AzureOpenAiEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        // See https://docs.langchain4j.dev/integrations/embedding-models/azure-open-ai/#spring-boot-1 for reference
        // of all properties that should be configurable.
        final String nonAzureKey = "non-azure-api-key";
        final String tokenKey = "token";
        long authCount = Stream.of("api-key", nonAzureKey, tokenKey)
            .filter(options::containsKey)
            .count();
        if (authCount != 1) {
            throw new IllegalArgumentException("Must specify exactly one of: api-key, non-azure-api-key, or token.");
        }

        final String deploymentName = options.get("deployment-name");
        if (deploymentName == null || deploymentName.trim().isEmpty()) {
            throw new IllegalArgumentException("deployment-name must be specified");
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

        if (options.containsKey(tokenKey)) {
            final String token = options.get(tokenKey);
            TokenCredential credential = request -> Mono.just(new AccessToken(token, OffsetDateTime.MAX));
            builder.tokenCredential(credential);
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
