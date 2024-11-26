/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GenericFilesImporterTest extends AbstractTest {

    private static final String PATH = "src/test/resources/mixed-files/hello*";

    @Test
    void test() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(PATH)
            // Including streaming just for smoke testing and manual inspection of log messages.
            .streaming()
            .to(options -> options
                .collectionsString("api-files,second-collection")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("api-files", 4);
        assertCollectionSize("second-collection", 4);
    }

    @Test
    void zipFile() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/mixed-files/goodbye.zip")
                .compressionType(CompressionType.ZIP))
            .to(options -> options
                .collections("files")
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriReplace(".*/mixed-files,''"))
            .execute();

        assertCollectionSize("files", 3);
        Stream.of("/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml").forEach(uri ->
            assertInCollections(uri, "files"));
    }

    @Test
    void documentType() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/mixed-files/hello.json")
            .to(options -> options
                .collectionsString("api-files,second-collection")
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriReplace(".*/mixed-files,''")
                .uriSuffix(".unknown")
                .documentType(GenericFilesImporter.DocumentType.JSON))
            .execute();

        String kind = getDatabaseClient().newServerEval()
            .xquery("xdmp:node-kind(doc('/hello.json.unknown')/node())")
            .evalAs(String.class);
        assertEquals("object", kind, "Forcing the document type to JSON should result in a document with " +
            "an unknown extension - in this case, 'unknown' - to be treated as JSON.");
    }

    @Test
    void badPath() {
        GenericFilesImporter command = Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("path/doesnt/exist");

        FluxException ex = assertThrowsFluxException(() -> command.execute());
        assertTrue(ex.getMessage().contains("Path does not exist"),
            "Unexpected message: " + ex.getMessage() + ". And I'm not sure we want this Spark-specific " +
                "exception to escape. Think we need for AbstractCommand to catch Throwable and look for a " +
                "Spark-specific exception. If one is found, we need a new generic 'Flux' runtime exception to throw " +
                "with the Spark-specific exception message.");
    }

    @Test
    void abortOnWriteFailure() {
        GenericFilesImporter command = Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.paths(PATH))
            .to(options -> options
                .abortOnWriteFailure(true)
                .permissionsString("not-a-real-role,update"));

        FluxException ex = assertThrows(FluxException.class, () -> command.execute());
        assertTrue(ex.getMessage().contains("Role does not exist"), "Unexpected error: " + ex.getMessage());
    }

    @Test
    void missingPath() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void splitXml() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/xml-file/namespaced-java-client-intro.xml")
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
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example:chunk")});
        doc.assertElementCount("/ex:my-chunk/ex:chunks/ex:chunk", 2);
    }

    @Test
    void splitJson() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/json-files/java-client-intro.json")
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
            .from("src/test/resources/json-files/java-client-intro.json")
            .to(writeOptions -> writeOptions
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/split-test.json")
                .splitter(splitterOptions -> splitterOptions.jsonPointers("/text", "/more-text")
                    .documentSplitterClassName("com.marklogic.flux.impl.importdata.splitter.CustomSplitter")
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
            .from("src/test/resources/json-files/java-client-intro.json")
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
}
