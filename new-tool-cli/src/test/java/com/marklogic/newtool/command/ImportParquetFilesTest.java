package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportParquetFilesTest extends AbstractTest {

    @Test
    void defaultSettingsSingleFile() {
        run(
            "import_parquet_files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--uriTemplate", "/parquet/{model}.json"
        );

        assertCollectionSize("parquet-test", 32);
        verifyCarDoc("/parquet/Datsun 710.json", 22.8, 4, 1);
        verifyCarDoc("/parquet/Ferrari Dino.json", 19.7, 5, 6);
        verifyCarDoc("/parquet/Toyota Corolla.json", 33.9, 4, 1);
    }

    @Test
    void defaultSettingsMultipleFileDifferentSchema_mergeTrue() {
        run(
            "import_parquet_files",
            "--path", "src/test/resources/parquet/related/*.parquet",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "-PmergeSchema=true",
            "--uriTemplate", "/parquet/{color}.json"
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
            run("import_parquet_files",
                "--path", "src/test/resources/parquet/individual/invalid.parquet",
                "--preview", "10"
            )
        );

        assertTrue(
            stderr.contains("Command failed, cause: [CANNOT_READ_FILE_FOOTER]"),
            "Sometimes Spark will throw a SparkException that wraps a SparkException, and it's the wrapped exception " +
                "that has the useful message in it. This test verifies that we use the message from the wrapped " +
                "SparkException, which is far more helpful for this particular failure. Unexpected stderr: " + stderr
        );
    }

    private void verifyColorDoc(String uri, String expectedNumber, String expectedColor, String expectedHex) {
        JsonNode doc = readJsonDocument(uri);

        if (expectedNumber != null) {
            assertEquals(expectedNumber, doc.get("number").asText());
            assertEquals(JsonNodeType.STRING, doc.get("number").getNodeType());
        } else {
            assertFalse(doc.has("number"));
        }

        assertEquals(expectedColor, doc.get("color").asText());

        if (expectedHex != null) {
            assertEquals(expectedHex, doc.get("hex").asText());
            assertEquals(JsonNodeType.STRING, doc.get("hex").getNodeType());
        } else {
            assertFalse(doc.has("hex"));
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
