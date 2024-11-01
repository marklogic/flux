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
    void minilm() {
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

    @Test
    void chunksAtTopLevel() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/top-chunks-test.json",
            "--embedder", "minilm",
            "--embedder-chunks-json-pointer", "",
            "--stacktrace"
        );

        JsonNode doc = readJsonDocument("/top-chunks-test.json");
        assertTrue(doc.has("embedding"), "Using '' should result in the whole document being treated as a chunk.");
        assertEquals(JsonNodeType.ARRAY, doc.get("embedding").getNodeType());
    }

    /**
     * This is expected to fail, as we're not going to depend on a valid Azure API key. But can at least make sure
     * that our Azure function is getting called and throwing an expected error.
     */
    @Test
    void azure() {
        assertStderrContains(() -> run(
                "import-files",
                "--path", "src/test/resources/json-files/java-client-intro.json",
                "--connection-string", makeConnectionString(),
                "--permissions", DEFAULT_PERMISSIONS,
                "--uri-replace", ".*/json-files,''",
                "--splitter-json-pointer", "/text",
                "--embedder", "azure",
                "-Eapi-key=doesnt-matter",
                "-Eendpoint=https://gpt-testing-custom-data1.openai.azure.com"
            ),
            "Access denied due to invalid subscription key"
        );
    }

    @Test
    void invalidClass() {
        assertStderrContains(() -> run(
                "import-files",
                "--path", "src/test/resources/json-files/java-client-intro.json",
                "--connection-string", makeConnectionString(),
                "--permissions", DEFAULT_PERMISSIONS,
                "--uri-replace", ".*/json-files,''",
                "--splitter-json-pointer", "/text",
                "--embedder", "not.valid.class"
            ),
            "Unable to instantiate class for creating an embedding model; class name: not.valid.class; " +
                "cause: Could not load class not.valid.class"
        );
    }
}
