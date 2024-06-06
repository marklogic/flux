package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParquetFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importParquetFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/parquet/related/*.parquet")
                .additionalOptions(Map.of("mergeSchema", "true")))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("parquet-test")
                .uriTemplate("/parquet/{color}.json"))
            .execute();

        assertCollectionSize("parquet-test", 6);
        JsonNode doc = readJsonDocument("/parquet/orange.json");
        assertEquals("#CF5A4F", doc.get("hex").asText(), "The inclusion of mergeSchema=true tells Spark Parquet " +
            "to merge the schemas of the different Parquet files it finds. Without that option, orange.json will " +
            "not have a 'hex' field.");
    }

    @Test
    void count() {
        long count = NT.importParquetFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/parquet/related/*.parquet")
                .additionalOptions(Map.of("mergeSchema", "true")))
            .writeDocuments(options -> options.collections("parquet-test"))
            .count();

        assertEquals(6, count);
        assertCollectionSize("parquet-test", 0);
    }

    @Test
    void missingPath() {
        ParquetFilesImporter importer = NT.importParquetFiles()
            .connectionString(makeConnectionString());

        NtException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
