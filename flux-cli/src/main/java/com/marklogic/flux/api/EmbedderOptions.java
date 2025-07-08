/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

/**
 * @since 1.2.0
 */
public interface EmbedderOptions {

    /**
     * Valid values:
     * <ul>
     *     <li>azure</li>
     *     <li>minilm</li>
     *     <li>ollama</li>
     * </ul>
     *
     * @param name either one of the abbreviations listed above, or the fully-qualified class name of an implementation of
     *             {@code java.util.Function<Map<String, String>, dev.langchain4j.model.embedding.EmbeddingModel>} or
     *             an abbreviation associated with the class name of an implementation.
     * @return
     */
    EmbedderOptions embedder(String name);

    EmbedderOptions chunksJsonPointer(String jsonPointer);

    EmbedderOptions textJsonPointer(String jsonPointer);

    EmbedderOptions chunksXPath(String xpath);

    EmbedderOptions textXPath(String xpath);

    EmbedderOptions embeddingName(String embeddingName);

    EmbedderOptions embeddingNamespace(String embeddingNamespace);

    EmbedderOptions batchSize(int batchSize);

    EmbedderOptions embedderOptions(Map<String, String> options);

    /**
     * @param prompt Optional prompt to prepend to each chunk of text before embedding.
     * @since 1.4.0
     */
    EmbedderOptions prompt(String prompt);
}
