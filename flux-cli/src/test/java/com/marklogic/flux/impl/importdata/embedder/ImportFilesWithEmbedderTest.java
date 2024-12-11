/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata.embedder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
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
            "--splitter-sidecar-permissions", "flux-test-role,read,flux-test-role,update",
            "--splitter-sidecar-collections", "sidecar-chunks",
            "--embedder", "minilm"
        );

        assertCollectionSize("sidecar-chunks", 2);
        verifyChunksHaveEmbeddings();
    }

    @Test
    void minilmNoNamespace() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-max-chunk-size", "500",
            "--splitter-sidecar-max-chunks", "2",
            "--splitter-sidecar-document-type", "xml",
            "--splitter-sidecar-xml-namespace", "",
            "--embedder", "minilm"
        );

        XmlNode doc = readXmlDocument("/java-client-intro.json-chunks-1.xml");
        doc.assertElementExists("Empty quotes is a valid value for a namespace", "/root/chunks/chunk[1]/embedding");
    }

    @Test
    void ollama() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-max-chunk-size", "500",
            "--splitter-sidecar-max-chunks", "2",
            "--embedder", "ollama",
            "-Ebase-url=http://localhost:8008",
            "-Emodel-name=all-minilm"
        );

        verifyChunksHaveEmbeddings();
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
                "-Eapi-key=doesnt-matter"
            ),
            "endpoint cannot be null or blank"
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

    private void verifyChunksHaveEmbeddings() {
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
