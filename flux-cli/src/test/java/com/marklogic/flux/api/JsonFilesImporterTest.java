package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importJsonFiles()
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
        JsonFilesImporter importer = Flux.importJsonFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
