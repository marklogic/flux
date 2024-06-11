package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ImportOrcFilesTest extends AbstractTest {

    @Test
    void orcFileTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test",
            "--uriTemplate", "/orc-test/{LastName}.json"
        );

        getUrisInCollection("orcFile-test", 15).forEach(this::verifyDocContent);
    }

    @Test
    void jsonRootName() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uriPrefix", "/orc-test",
            "--jsonRootName", "myOrcData",
            "--uriTemplate", "/orc/{/myOrcData/LastName}.json"
        );

        JsonNode doc = readJsonDocument("/orc/Humbee.json");
        assertEquals("Aida", doc.get("myOrcData").get("ForeName").asText());
    }

    @Test
    void orcFileWithCompressionTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "compression-test",
            "--uriTemplate", "/orc-compressed-test{LastName}.json",
            "-Pcompression=snappy"
        );

        getUrisInCollection("compression-test", 15).forEach(this::verifyDocContent);
    }

    @Test
    void badConfigurationItem() {
        String stderr = runAndReturnStderr(() ->
            run(
                "import_orc_files",
                "--path", "src/test/resources/orc-files",
                "--connectionString", makeConnectionString(),
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
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--path", "src/test/resources/avro/colors.avro",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-data"
        ));

        assertCollectionSize("The authors.orc file should have been processed correctly", "orc-data", 15);
        assertFalse(stderr.contains("Command failed"), "The command should default to ignoreCorruptFiles=true for " +
            "reading ORC files so that invalid files do not cause the command to fail; stderr: " + stderr);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import_parquet_files",
            "--path", "src/test/resources/avro/colors.avro",
            "--abortOnReadFailure",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        ));

        assertTrue(stderr.contains("Command failed") && stderr.contains("Could not read footer for file"),
            "The command should have failed because Spark could not read the footer of the invalid Avro file; " +
                "stderr: " + stderr);
    }

    private void verifyDocContent(String uri) {
        JsonNode doc = readJsonDocument(uri);
        assertTrue(doc.has("CitationID"));
        assertTrue(doc.has("LastName"));
        assertTrue(doc.has("ForeName"));
    }
}
