/*
 * Copyright (c) 2010-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.bedrock.BedrockCohereEmbeddingModel;
import dev.langchain4j.model.bedrock.BedrockTitanEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.util.Map;
import java.util.function.Function;

public class BedrockEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        return "titan".equals(options.get("type")) ?
            buildTitanEmbeddingModel(options) :
            buildCohereEmbeddingModel(options);
    }

    private EmbeddingModel buildTitanEmbeddingModel(Map<String, String> options) {
        BedrockTitanEmbeddingModel.BedrockTitanEmbeddingModelBuilder builder = BedrockTitanEmbeddingModel.builder()
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .model(options.get("model"));

        if (options.containsKey("region")) {
            builder.region(Region.of(options.get("region")));
        }

        if (options.containsKey("dimensions")) {
            builder.dimensions(Integer.parseInt(options.get("dimensions")));
        }

        if (options.containsKey("normalize")) {
            builder.normalize(Boolean.parseBoolean(options.get("normalize")));
        }

        return builder.build();
    }

    private EmbeddingModel buildCohereEmbeddingModel(Map<String, String> options) {
        BedrockCohereEmbeddingModel.Builder builder = BedrockCohereEmbeddingModel.builder()
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .model(options.get("model"))
            .inputType(options.get("inputType"))
            .truncate(options.get("truncate"))
            .maxRetries(getInteger(options, "maxRetries"));

        if (options.containsKey("region")) {
            builder.region(Region.of(options.get("region")));
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
