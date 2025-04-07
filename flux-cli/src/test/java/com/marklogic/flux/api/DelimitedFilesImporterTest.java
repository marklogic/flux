/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelimitedFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importDelimitedFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/no-header.csv")
                .additionalOptions(Map.of("header", "false")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("no-header")
                .uriTemplate("/no-header/{_c0}.json"))
            .execute();

        assertCollectionSize("no-header", 2);
        JsonNode doc = readJsonDocument("/no-header/1.json");
        assertEquals(1, doc.get("_c0").asInt(), "If no header row is present, Spark will name columns starting " +
            "with '_c0'.");
    }

    @Test
    void missingPath() {
        DelimitedFilesImporter importer = Flux.importDelimitedFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
