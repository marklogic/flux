package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertTrue(ex.getMessage().contains("Unrecognized token"), "Unexpected error: " + ex.getMessage() + "; " +
            "verifying that the additionalOptions map is processed, which should cause an error due to the " +
            "invalid Avro schema.");
    }

    @Test
    void missingPath() {
        AvroFilesImporter importer = Flux.importAvroFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
