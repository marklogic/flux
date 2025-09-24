/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractJava17Test;
import com.marklogic.junit5.XmlNode;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitWithApiTest extends AbstractJava17Test {

    @Test
    void splitXml() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/xml-file/namespaced-java-client-intro.xml")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("files")
                .uriReplace(".*/xml-file,''")
                .splitter(splitterOptions -> splitterOptions
                    .xpath("/ex:root/ex:text/text()")
                    .xmlNamespaces("ex", "org:example")
                    .maxChunkSize(500)
                    .maxOverlapSize(0)
                    .outputCollections("xml-chunks")
                    .outputDocumentType(SplitterOptions.ChunkDocumentType.XML)
                    .outputMaxChunks(2)
                    .outputPermissionsString("flux-test-role,read,flux-test-role,update,qconsole-user,read")
                    .outputRootName("my-chunk")
                    .outputUriPrefix("/chunk/")
                    .outputUriSuffix("/chunk.xml")
                    .outputXmlNamespace("org:example:chunk")
                )
            ).execute();

        String uri = getUrisInCollection("xml-chunks", 2).get(0);
        assertTrue(uri.startsWith("/chunk/"));
        assertTrue(uri.endsWith("/chunk.xml"));

        XmlNode doc = readXmlDocument(uri);
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ch", "org:example:chunk")});
        doc.assertElementCount("/ch:my-chunk/ch:chunks/ch:chunk", 2);
    }

    @Test
    void splitJson() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/split-test.json")
                .splitter(splitterOptions -> splitterOptions.jsonPointers("/text", "/more-text"))
            ).execute();

        JsonNode doc = readJsonDocument("/split-test.json");
        assertEquals(2, doc.get("chunks").size(), "Should get 2 chunks with the default max chunk size of 1000.");
    }

    @Test
    void splitWithCustomClass() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/split-test.json")
                .splitter(splitterOptions -> splitterOptions.jsonPointers("/text", "/more-text")
                    .documentSplitterClassName("com.marklogic.flux.impl.importdata.CustomSplitter")
                    .documentSplitterClassOptions(Map.of("textToReturn", "just testing")))
            ).execute();

        ArrayNode chunks = (ArrayNode) readJsonDocument("/split-test.json").get("chunks");
        assertEquals(1, chunks.size());
        assertEquals("just testing", chunks.get(0).get("text").asText());
    }

    @Test
    void splitAndEmbed() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/split-test.json")
                .splitter(splitterOptions -> splitterOptions.jsonPointers("/text"))
                .embedder(embedderOptions -> embedderOptions.embedder("minilm"))
            ).execute();

        JsonNode doc = readJsonDocument("/split-test.json");
        assertEquals(2, doc.get("chunks").size(), "Should get 2 chunks with the default max chunk size of 1000.");

        doc.get("chunks").forEach(chunk -> {
            assertTrue(chunk.has("text"));
            assertTrue(chunk.has("embedding"));
            assertEquals(JsonNodeType.ARRAY, chunk.get("embedding").getNodeType());
        });
    }

    @Test
    void splitAndEmbedWithFullClassName() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/split-test.json")
                .splitter(splitterOptions -> splitterOptions.jsonPointers("/text"))
                .embedder(embedderOptions -> embedderOptions.embedder("com.marklogic.flux.langchain4j.embedding.MinilmEmbeddingModelFunction"))
            ).execute();

        JsonNode doc = readJsonDocument("/split-test.json");
        assertEquals(2, doc.get("chunks").size(), "Should get 2 chunks with the default max chunk size of 1000.");
    }

    @Test
    void copyDocuments() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("../flux-cli/src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/document-to-split.json")
            ).execute();

        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.uris("/document-to-split.json"))
            .to(options -> options
                .uriPrefix("/copied")
                .permissionsString(DEFAULT_PERMISSIONS)
                .splitter(splitterOptions -> splitterOptions
                    .jsonPointers("/text")
                    .maxChunkSize(1500)
                    .regex("MarkLogic")
                    .joinDelimiter(" "))
            )
            .execute();

        JsonNode doc = readJsonDocument("/copied/document-to-split.json");
        assertEquals(2, doc.get("chunks").size(), "Expecting 2 chunks based on the default max chunk " +
            "size of 1000.");

        String secondChunk = doc.get("chunks").get(1).get("text").asText();
        assertEquals("Server. You can also create your own on another port. For details, see Choose a REST API Instance.",
            secondChunk, "Verifying that the text was split based on using a trivial regex.");
    }

}
