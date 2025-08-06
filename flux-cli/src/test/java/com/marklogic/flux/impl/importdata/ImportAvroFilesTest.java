/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportAvroFilesTest extends AbstractTest {

    @Test
    void defaultSettingsMultipleFiles() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "--uri-template", "/avro/{color}.json",

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "2",

            // Including this to ensure a valid -C option doesn't cause an error.
            "-Cspark.sql.avro.filterPushdown.enabled=false"
        );

        assertCollectionSize("avro-test", 6);
        verifyColorDoc("/avro/blue.json", "1", "blue", true);
        verifyColorDoc("/avro/red.json", "2", "red", false);
        verifyColorDoc("/avro/green.json", "3", "green", true);
        verifyColorDoc("/avro/puce.json", "4", "puce", true);
        verifyColorDoc("/avro/ecru.json", "5", "ecru", false);
        verifyColorDoc("/avro/mauve.json", "6", "mauve", true);
    }

    @Test
    void uriIncludeFilePath() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro/colors.avro",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--xml-root-name", "myAvroData",
            "--collections", "avro-test",
            "--uri-include-file-path",
            "--uri-replace", ".*/resources,''"
        );

        getUrisInCollection("avro-test", 3).forEach(uri -> {
            assertTrue(uri.startsWith("/avro/colors.avro/"), "Actual URI: " + uri);
            assertTrue(uri.endsWith(".xml"), "Actual URI: " + uri);
        });
    }

    @Test
    void jsonRootName() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--json-root-name", "myAvroData",
            "--uri-template", "/avro/{/myAvroData/color}.json"
        );

        JsonNode doc = readJsonDocument("/avro/blue.json");
        assertEquals(1, doc.get("myAvroData").get("number").asInt());
    }

    @Test
    void aggregate() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro",
            "--group-by", "flag", // Weird, but effective for our test data.
            "--aggregate", "values=number,color",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-avro",
            "--uri-template", "/avro/{flag}.json"
        );

        assertCollectionSize("my-avro", 2);
        JsonNode doc = readJsonDocument("/avro/true.json");
        ArrayNode values = (ArrayNode) doc.get("values");
        assertEquals(4, values.size());
        for (int i = 0; i < 4; i++) {
            JsonNode object = values.get(i);
            assertTrue(object.has("number"));
            assertTrue(object.has("color"));
        }
    }

    @Test
    void ignoreExtension() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "-PignoreExtension=false",
            "--uri-template", "/avro/{color}.json"
        );

        assertCollectionSize("avro-test", 3);
        verifyColorDoc("/avro/blue.json", "1", "blue", true);
        verifyColorDoc("/avro/red.json", "2", "red", false);
        verifyColorDoc("/avro/green.json", "3", "green", true);
    }

    @Test
    void badConfigurationItem() {
        String stderr = runAndReturnStderr(
            "import-avro-files",
            "--path", "src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "-Cspark.sql.parquet.filterPushdown=invalid-value"
        );

        assertTrue(stderr.contains("spark.sql.parquet.filterPushdown should be boolean, but was invalid-value"),
            "This test verifies that spark.sql dynamic params are added to the Spark conf. An invalid value is used " +
                "to verify this, as its inclusion in the Spark conf should cause an error. Actual stderr: " + stderr);
    }

    @Test
    void dontAbortOnReadFailure() {
        String stderr = runAndReturnStderr(
            "import-avro-files",
            "--path", "src/test/resources/avro/colors.avro",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-data"
        );

        assertCollectionSize("The colors.avro file should have been processed correctly", "avro-data", 3);
        assertFalse(stderr.contains("Command failed"), "The command should default to ignoreCorruptFiles=true for " +
            "reading Avro files so that invalid files do not cause the command to fail; stderr: " + stderr);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(
            "import-avro-files",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        );

        assertTrue(stderr.contains("Error: Not an Avro data file"), "Actual stderr: " + stderr);
    }

    private void verifyColorDoc(String uri, String expectedNumber, String expectedColor, boolean expectedFlag) {
        JsonNode doc = readJsonDocument(uri);

        assertEquals(expectedNumber, doc.get("number").asText());
        assertEquals(JsonNodeType.STRING, doc.get("number").getNodeType());

        assertEquals(expectedColor, doc.get("color").asText());
        assertEquals(JsonNodeType.STRING, doc.get("color").getNodeType());

        assertEquals(expectedFlag, doc.get("flag").asBoolean());
        assertEquals(JsonNodeType.BOOLEAN, doc.get("flag").getNodeType());
    }
}
