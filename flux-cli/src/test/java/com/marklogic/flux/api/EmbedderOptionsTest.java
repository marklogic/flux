/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.impl.importdata.EmbedderParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class EmbedderOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        AtomicReference<EmbedderOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.embedder(embedderOptions -> {
                embedderOptions
                    .embedder("minilm")
                    .chunksJsonPointer("/json/chunks")
                    .chunksXPath("/xml/chunks")
                    .embedderOptions(Map.of("myKey", "myValue"))
                    .embeddingName("my-embedding")
                    .embeddingNamespace("org:example")
                    .batchSize(5)
                    .base64Encode()
                    .prompt("Just testing")
                    .textJsonPointer("/json/text")
                    .textXPath("/xml/text");
                reference.set(embedderOptions);
            }));

        EmbedderParams params = (EmbedderParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, "com.marklogic.flux.langchain4j.embedding.MinilmEmbeddingModelFunction",
            Options.WRITE_EMBEDDER_CHUNKS_JSON_POINTER, "/json/chunks",
            Options.WRITE_EMBEDDER_CHUNKS_XPATH, "/xml/chunks",
            Options.WRITE_EMBEDDER_MODEL_FUNCTION_OPTION_PREFIX + "myKey", "myValue",
            Options.WRITE_EMBEDDER_EMBEDDING_NAME, "my-embedding",
            Options.WRITE_EMBEDDER_EMBEDDING_NAMESPACE, "org:example",
            Options.WRITE_EMBEDDER_TEXT_JSON_POINTER, "/json/text",
            Options.WRITE_EMBEDDER_TEXT_XPATH, "/xml/text",
            Options.WRITE_EMBEDDER_BATCH_SIZE, "5",
            Options.WRITE_EMBEDDER_PROMPT, "Just testing",
            Options.WRITE_EMBEDDER_BASE64_ENCODE, "true"
        );
    }
}
