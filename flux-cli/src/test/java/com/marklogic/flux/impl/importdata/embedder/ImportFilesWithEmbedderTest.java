/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata.embedder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportFilesWithEmbedderTest extends AbstractTest {

    @Test
    void test() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-max-chunk-size", "500",
            "--splitter-sidecar-max-chunks", "2",
            "--embedder", "minilm"
        );

        Stream.of("/java-client-intro.json-chunks-1.json", "/java-client-intro.json-chunks-2.json").forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertEquals("/java-client-intro.json", doc.get("source-uri").asText());
            assertTrue(doc.has("chunks"));
            ArrayNode chunks = (ArrayNode) doc.get("chunks");
            assertEquals(2, chunks.size());

            chunks.forEach(chunk -> {
                assertTrue(chunk.has("text"));
                assertTrue(chunk.has("embedding"));
                assertEquals(JsonNodeType.ARRAY, chunk.get("embedding").getNodeType());
            });
        });
    }
}
