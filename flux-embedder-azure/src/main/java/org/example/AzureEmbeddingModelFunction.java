/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.Map;
import java.util.function.Function;

public class AzureEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    // TODO Try to package this into the Flux zip?
    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        return AzureOpenAiEmbeddingModel.builder()
            .logRequestsAndResponses(true)
            .apiKey(options.get("api-key"))
            .deploymentName(options.get("deployment-name"))
            .endpoint(options.get("endpoint"))
            .build();
    }
}
