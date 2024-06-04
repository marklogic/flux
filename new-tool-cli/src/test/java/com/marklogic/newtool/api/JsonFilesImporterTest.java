package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importJsonFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/delimited-files/custom-delimiter-json.txt")
                .jsonLines(true)
                .additionalOptions(Map.of("lineSep", ":\n")))
            .writeDocuments(options -> options
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
        JsonFilesImporter importer = NT.importJsonFiles()
            .connectionString(makeConnectionString());

        NtException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
