/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.langchain4j.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

import java.util.Map;
import java.util.function.Function;

public class MinilmEmbeddingModelFunction implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
