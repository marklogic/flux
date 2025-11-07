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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvroFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importAvroFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/avro/*")
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("avro-test")
                .uriTemplate("/avro/{color}.json"))
            .execute();

        assertCollectionSize("avro-test", 6);
    }

    @Test
    void badAvroOption() {
        AvroFilesImporter importer = Flux.importAvroFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/avro/*")
                .additionalOptions(Map.of("avroSchema", "not-a-valid-schema")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("avro-test")
                .uriTemplate("/avro/{color}.json"));

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertTrue(ex.getMessage().contains("Unrecognized token"), "Unexpected error: " + ex.getMessage() + "; " +
            "verifying that the additionalOptions map is processed, which should cause an error due to the " +
            "invalid Avro schema.");
    }

    @Test
    void missingPath() {
        AvroFilesImporter importer = Flux.importAvroFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void aggregate() {
        Flux.importAvroFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/avro")
                .groupBy("flag")
                .aggregateColumns("values", "number", "color")
                .aggregateOrderBy("values", "number", false)
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("my-avro")
                .uriTemplate("/avro/{flag}.json")
            )
            .execute();

        assertCollectionSize("my-avro", 2);
        JsonNode doc = readJsonDocument("/avro/true.json");
        ArrayNode values = (ArrayNode) doc.get("values");
        assertEquals(4, values.size());

        assertEquals(6, values.get(0).get("number").asInt());
        assertEquals(4, values.get(1).get("number").asInt());
        assertEquals(3, values.get(2).get("number").asInt());
        assertEquals(1, values.get(3).get("number").asInt());
    }
}
