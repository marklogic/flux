/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateJsonFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/custom-delimiter-json.txt")
                .jsonLines(true)
                .additionalOptions(Map.of("lineSep", ":\n")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("custom-delimiter")
                .uriTemplate("/custom-delimiter/{firstName}.json"))
            .execute();

        assertCollectionSize("custom-delimiter", 3);
        Stream.of(
            "/custom-delimiter/firstName-1.json", "/custom-delimiter/firstName-2.json", "/custom-delimiter/firstName-3.json"
        ).forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertTrue(doc.has("firstName"));
            assertTrue(doc.has("lastName"));
        });
    }

    @Test
    void missingPath() {
        AggregateJsonFilesImporter importer = Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
