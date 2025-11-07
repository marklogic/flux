/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrcFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importOrcFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/orc-files/authors.orc")
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test")
                .uriTemplate("/orc-test/{LastName}.json"))
            .execute();

        assertCollectionSize("orc-test", 15);
    }

    @Test
    void badOption() {
        OrcFilesImporter importer = Flux.importOrcFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/orc-files/authors.orc")
                .additionalOptions(Map.of("mergeSchema", "not-valid-value"))
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test"));

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("For input string: \"not-valid-value\"", ex.getMessage(), "Expecting a failure due to the " +
            "invalid value for the ORC 'mergeSchema' option.");
    }

    @Test
    void missingPath() {
        OrcFilesImporter importer = Flux.importOrcFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void aggregate() {
        Flux.importOrcFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/orc-files/authors.orc")
                .groupBy("CitationID")
                .aggregateColumns("names", "ForeName", "LastName")
                .aggregateOrderBy("names", "LastName", true)
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test")
                .uriTemplate("/orc-test/{CitationID}.json")
            )
            .execute();

        assertCollectionSize("Expecting 1 doc for each CitationID", "orc-test", 5);

        JsonNode doc = readJsonDocument("/orc-test/1.json");
        JsonNode names = doc.get("names");
        assertEquals("Awton", names.get(0).get("LastName").asText());
        assertEquals("Bernadzki", names.get(1).get("LastName").asText());
        assertEquals("Canham", names.get(2).get("LastName").asText());
        assertEquals("Golby", names.get(3).get("LastName").asText());
    }
}
