/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportParquetFilesTest extends AbstractTest {

    @Test
    void defaultSettingsSingleFile() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--total-thread-count", "1",
            "--uri-template", "/parquet/{model}.json",

            // Including these for manual verification of progress logging.
            "--batch-size", "5",
            "--log-progress", "10"
        );

        assertCollectionSize("parquet-test", 32);
        verifyCarDoc("/parquet/Datsun 710.json", 22.8, 4, 1);
        verifyCarDoc("/parquet/Ferrari Dino.json", 19.7, 5, 6);
        verifyCarDoc("/parquet/Toyota Corolla.json", 33.9, 4, 1);
    }

    @Test
    void uriIncludeFilePath() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--uri-include-file-path",
            "--uri-replace", ".*/individual,''"
        );

        getUrisInCollection("parquet-test", 32).forEach(uri -> {
            assertTrue(uri.startsWith("/cars.parquet/"), "Actual URI: " + uri);
            assertTrue(uri.endsWith(".json"), "Actual URI: " + uri);
        });
    }

    @Test
    void aggregate() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--group-by", "cyl",
            "--aggregate", "models=model,mpg",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "cyl-test",
            "--uri-template", "/parquet/{cyl}.json"
        );

        assertCollectionSize("Expecting 3 documents, since there are 3 values for cyl - 4, 6, and 8", "cyl-test", 3);
        JsonNode doc = readJsonDocument("/parquet/4.json");
        assertEquals(4, doc.get("cyl").asInt());
        ArrayNode models = (ArrayNode) doc.get("models");
        assertEquals(11, models.size(), "Expecting 11 models to have cyl=4");
        for (int i = 0; i < 11; i++) {
            JsonNode model = models.get(i);
            assertTrue(model.has("model"));
            assertTrue(model.has("mpg"));
        }
    }

    @Test
    void count() {
        String stderr = runAndReturnStderr(() -> run(
            "import-parquet-files",
            "--count",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test"
        ));

        assertCollectionSize("No data should be written when --count is included",
            "parquet-test", 0);
        assertFalse(stderr.contains("Command failed"), "No error should have occurred; the count of rows read should " +
            "have been logged, which we unfortunately don't yet know how to make assertions against.");
    }

    @Test
    void jsonRootName() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--json-root-name", "car",
            "--uri-template", "/parquet/{/car/model}.json"
        );

        JsonNode doc = readJsonDocument("/parquet/Toyota Corolla.json");
        assertEquals("33.9", doc.get("car").get("mpg").asText());
    }

    @Test
    void defaultSettingsMultipleFileDifferentSchema_mergeTrue() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/related/*.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "-PmergeSchema=true",
            "--uri-template", "/parquet/{color}.json"
        );

        assertCollectionSize("parquet-test", 6);
        // Setting "mergeSchema" to "true" means that the schema can grow as new files are read into the data frame.
        // So, columns in later files will be included in the resulting schema and therefore in the resulting rows.
        // When mergeSchema is false (the default) the schema is static and based on the first file read into the data frame.
        // In that case, new columns in subsequent files are completely ignored.

        // "blue" is in the first file which does not have a "hex" column,
        // so there is no "hex" column in the schema (initially) or value in the document.
        verifyColorDoc("/parquet/blue.json", "1", "blue", null);

        // "purple" is in the second file and has a "hex" column, with mergeSchema set to true, the new column is added
        // to the schema and value in the document.
        verifyColorDoc("/parquet/purple.json", null, "purple", "#A020F0");
    }

    @Test
    void invalidParquetFile() {
        String stderr = runAndReturnStderr(() ->
            run("import-parquet-files",
                "--connection-string", makeConnectionString(),
                "--path", "src/test/resources/parquet/individual/invalid.parquet",
                "--abort-on-read-failure"
            )
        );

        assertTrue(
            stderr.contains("Command failed, cause: [CANNOT_READ_FILE_FOOTER]"),
            "Sometimes Spark will throw a SparkException that wraps a SparkException, and it's the wrapped exception " +
                "that has the useful message in it. This test verifies that we use the message from the wrapped " +
                "SparkException, which is far more helpful for this particular failure. Unexpected stderr: " + stderr
        );
    }

    @Test
    void badConfigurationItem() {
        String stderr = runAndReturnStderr(() ->
            run(
                "import-parquet-files",
                "--path", "src/test/resources/parquet/individual/cars.parquet",
                "--connection-string", makeConnectionString(),
                "--permissions", DEFAULT_PERMISSIONS,
                "-Cspark.sql.parquet.filterPushdown=invalid-value"
            )
        );

        assertTrue(stderr.contains("spark.sql.parquet.filterPushdown should be boolean, but was invalid-value"),
            "This test verifies that spark.sql dynamic params are added to the Spark conf. An invalid value is used " +
                "to verify this, as its inclusion in the Spark conf should cause an error. Actual stderr: " + stderr);
    }

    @Test
    void dontAbortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--path", "src/test/resources/avro/colors.avro",
            // Without mergeSchema=true, Spark will throw an error of "Unable to infer schema for Parquet". This seems
            // to occur if there's at least one bad file. With mergeSchema=true,
            "-PmergeSchema=true",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-cars"
        ));

        assertCollectionSize("The cars.parquet file should still have been processed.", "parquet-cars", 32);
        assertFalse(stderr.contains("Command failed"), "The command should default to ignoreCorruptFiles=true for " +
            "reading Parquet files so that invalid files do not cause the command to fail; stderr: " + stderr);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--path", "src/test/resources/avro/colors.avro",
            "--abort-on-read-failure",
            // This is kept here to ensure the command fails because it could read the Avro file and not because
            // Spark could not infer a schema.
            "-PmergeSchema=true",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        ));

        assertTrue(stderr.contains("Command failed") && stderr.contains("Could not read footer for file"),
            "The command should have failed because Spark could not read the footer of the invalid Avro file; " +
                "stderr: " + stderr);
    }

    private void verifyColorDoc(String uri, String expectedNumber, String expectedColor, String expectedHex) {
        JsonNode doc = readJsonDocument(uri);

        if (expectedNumber != null) {
            assertEquals(expectedNumber, doc.get("number").asText());
            assertEquals(JsonNodeType.STRING, doc.get("number").getNodeType());
        } else {
            assertEquals(JsonNodeType.NULL, doc.get("number").getNodeType());
        }

        assertEquals(expectedColor, doc.get("color").asText());

        if (expectedHex != null) {
            assertEquals(expectedHex, doc.get("hex").asText());
            assertEquals(JsonNodeType.STRING, doc.get("hex").getNodeType());
        } else {
            assertEquals(JsonNodeType.NULL, doc.get("hex").getNodeType());
        }
    }

    private void verifyCarDoc(String uri, double expectedMpg, int expectedGear, int expectedCarb) {
        JsonNode doc = readJsonDocument(uri);

        assertEquals(expectedMpg, doc.get("mpg").asDouble());
        assertEquals(JsonNodeType.NUMBER, doc.get("mpg").getNodeType());

        assertEquals(expectedGear, doc.get("gear").asInt());
        assertEquals(JsonNodeType.NUMBER, doc.get("gear").getNodeType());

        assertEquals(expectedCarb, doc.get("carb").asInt());
        assertEquals(JsonNodeType.NUMBER, doc.get("carb").getNodeType());
    }
}
