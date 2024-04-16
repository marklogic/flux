package com.marklogic.newtool.command.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ImportOrcFilesTest extends AbstractTest {

    @Test
    void orcFileTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/orc-file.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test",
            "--uriPrefix", "/orc-test"
        );

        assertCollectionSize("orcFile-test", 5);
        verifyDocContent("/orc-test/author/author12.json");
        verifyDocContent("/orc-test/author/author2.json");
        verifyDocContent("/orc-test/author/author5.json");
        verifyDocContent("/orc-test/author/author6.json");
        verifyDocContent("/orc-test/author/author9.json");
    }

    @Test
    void jsonRootName() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--clientUri", makeClientUri(),
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
            "--path", "src/test/resources/orc-files/orc-file.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFileWithCompressionTest-test",
            "--uriPrefix", "/orc-compressed-test",
            "-Pcompression=snappy"
        );

        assertCollectionSize("orcFileWithCompressionTest-test", 5);
        verifyDocContent("/orc-compressed-test/author/author12.json");
        verifyDocContent("/orc-compressed-test/author/author2.json");
        verifyDocContent("/orc-compressed-test/author/author5.json");
        verifyDocContent("/orc-compressed-test/author/author6.json");
        verifyDocContent("/orc-compressed-test/author/author9.json");
    }

    @Test
    void badConfigurationItem() {
        String stderr = runAndReturnStderr(() ->
            run(
                "import_orc_files",
                "--path", "src/test/resources/orc-files",
                "--clientUri", makeClientUri(),
                "--permissions", DEFAULT_PERMISSIONS,
                "-Pspark.sql.parquet.filterPushdown=invalid-value"
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS
        ));

        assertTrue(stderr.contains("Command failed") && stderr.contains("Could not read footer for file"),
            "The command should have failed because Spark could not read the footer of the invalid Avro file; " +
                "stderr: " + stderr);
    }

    private void verifyDocContent(String uri) {
        JSONDocumentManager documentManager = getDatabaseClient().newJSONDocumentManager();
        String docContent = documentManager.read(uri).next().getContent(new StringHandle()).toString();
        assertTrue(docContent.contains("CitationID"));
        assertTrue(docContent.contains("LastName"));
        assertTrue(docContent.contains("ForeName"));
    }
}
