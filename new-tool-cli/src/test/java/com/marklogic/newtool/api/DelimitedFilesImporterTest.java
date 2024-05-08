package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelimitedFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importDelimitedFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/delimited-files/no-header.csv")
                .additionalOptions(Map.of("header", "false")))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("no-header")
                .uriTemplate("/no-header/{_c0}.json"))
            .execute();

        assertCollectionSize("no-header", 2);
        JsonNode doc = readJsonDocument("/no-header/1.json");
        assertEquals(1, doc.get("_c0").asInt(), "If no header row is present, Spark will name columns starting " +
            "with '_c0'.");
    }
}
