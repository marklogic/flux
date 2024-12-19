/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentCopierTest extends AbstractTest {

    @Test
    void sameConnectionString() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content"))
            .to(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .batchSize(5)
                .logProgress(5)
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }

    @Test
    void withOutputConnection() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content"))
            .outputConnection(options -> options
                .host(getDatabaseClient().getHost())
                .port(getDatabaseClient().getPort())
                .username(DEFAULT_USER)
                .password(DEFAULT_PASSWORD))
            .to(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }

    @Test
    void splitterTest() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/json-files/java-client-intro.json")
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
