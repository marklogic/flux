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

class ParquetFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importParquetFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/parquet/related/*.parquet")
                .additionalOptions(Map.of("mergeSchema", "true")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("parquet-test")
                .uriTemplate("/parquet/{color}.json")
                .batchSize(3)
                .logProgress(6))
            .execute();

        assertCollectionSize("parquet-test", 6);
        JsonNode doc = readJsonDocument("/parquet/orange.json");
        assertEquals("#CF5A4F", doc.get("hex").asText(), "The inclusion of mergeSchema=true tells Spark Parquet " +
            "to merge the schemas of the different Parquet files it finds. Without that option, orange.json will " +
            "not have a 'hex' field.");
    }

    @Test
    void count() {
        long count = Flux.importParquetFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/parquet/related/*.parquet")
                .additionalOptions(Map.of("mergeSchema", "true")))
            .to(options -> options.collections("parquet-test"))
            .count();

        assertEquals(6, count);
        assertCollectionSize("parquet-test", 0);
    }

    @Test
    void missingPath() {
        ParquetFilesImporter importer = Flux.importParquetFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void aggregate() {
        Flux.importParquetFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/parquet/individual/cars.parquet")
                .groupBy("cyl")
                .aggregateColumns("models", "model", "mpg")
                .orderAggregation("models", "mpg", true)
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("cyl-test")
                .uriTemplate("/parquet/{cyl}.json")
            )
            .execute();

        assertCollectionSize("Expecting 3 documents, since there are 3 values for cyl - 4, 6, and 8", "cyl-test", 3);
        JsonNode doc = readJsonDocument("/parquet/4.json");
        assertEquals(4, doc.get("cyl").asInt());
        ArrayNode models = (ArrayNode) doc.get("models");
        assertEquals(11, models.size(), "Expecting 11 models to have cyl=4");

        assertEquals(21.4, models.get(0).get("mpg").asDouble());
        assertEquals(33.9, models.get(10).get("mpg").asDouble());
    }
}
