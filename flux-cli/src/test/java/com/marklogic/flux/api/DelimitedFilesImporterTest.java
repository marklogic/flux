/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    @Test
    void aggregate() {
        Flux.importDelimitedFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/join-rows.csv")
                .groupBy("number")
                .aggregateColumns("objects", "color", "flag")
                .aggregateOrderBy("objects", "color", true)
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("my-csv")
                .uriTemplate("/my-csv/{number}.json")
            )
            .execute();

        JsonNode doc = readJsonDocument("/my-csv/1.json");
        assertEquals(1, doc.get("number").asInt());
        ArrayNode objects = (ArrayNode) doc.get("objects");
        assertEquals(2, objects.size());
        assertEquals("blue", objects.get(0).get("color").asText());
        assertEquals(true, objects.get(0).get("flag").asBoolean());
        assertEquals("yellow", objects.get(1).get("color").asText());
        assertEquals(true, objects.get(1).get("flag").asBoolean());
    }
}
