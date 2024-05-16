package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.apache.avro.SchemaParseException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvroFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importAvroFiles()
            .connectionString(makeConnectionString())
            .readFiles("src/test/resources/avro/*")
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("avro-test")
                .uriTemplate("/avro/{color}.json"))
            .execute();

        assertCollectionSize("avro-test", 6);
    }

    @Test
    void badAvroOption() {
        AvroFilesImporter importer = NT.importAvroFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/avro/*")
                .additionalOptions(Map.of("avroSchema", "not-a-valid-schema")))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("avro-test")
                .uriTemplate("/avro/{color}.json"));

        NtException ex = assertThrowsNtException(() -> importer.execute());
        assertTrue(ex.getMessage().contains("Unrecognized token"), "Unexpected error: " + ex.getMessage() + "; " +
            "verifying that the additionalOptions map is processed, which should cause an error due to the " +
            "invalid Avro schema.");
    }

}
