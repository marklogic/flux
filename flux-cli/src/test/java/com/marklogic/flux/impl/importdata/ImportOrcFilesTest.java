/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ImportOrcFilesTest extends AbstractTest {

    @Test
    void orcFileTest() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test",
            "--uri-template", "/orc-test/{LastName}.json",

            // Including these for manual verification of progress logging.
            "--batch-size", "5",
            "--log-progress", "5",

            // Including this to ensure a valid -C option doesn't cause an error.
            "-Cspark.sql.orc.filterPushdown=false"
        );

        getUrisInCollection("orcFile-test", 15).forEach(this::verifyDocContent);
    }

    @Test
    void uriIncludeFilePath() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test",
            "--uri-include-file-path",
            "--uri-replace", ".*resources,''"
        );

        getUrisInCollection("orcFile-test", 15).forEach(uri -> {
            assertTrue(uri.startsWith("/orc-files/authors.orc/"), "Actual URI: " + uri);
            assertTrue(uri.endsWith(".json"), "Actual URI: " + uri);
        });
    }

    @Test
    void splitterSmokeTest() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/orc-test/{LastName}.json",
            "--splitter-json-pointer", "/LastName"
        );

        JsonNode doc = readJsonDocument("/orc-test/Awton.json");
        assertEquals("Awton", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void aggregate() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--group-by", "CitationID",
            "--aggregate", "names=ForeName,LastName",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-test",
            "--uri-template", "/orc-test/{CitationID}.json"
        );

        assertCollectionSize("Expecting 1 doc for each CitationID", "orc-test", 5);
        for (int i = 1; i <= 5; i++) {
            JsonNode doc = readJsonDocument("/orc-test/" + i + ".json");
            assertEquals(i, doc.get("CitationID").asInt());
            ArrayNode names = (ArrayNode) doc.get("names");
            for (int j = 0; j < names.size(); j++) {
                JsonNode name = names.get(j);
                assertTrue(name.has("ForeName"));
                assertTrue(name.has("LastName"));
            }
        }
    }


    @Test
    void jsonRootName() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-prefix", "/orc-test",
            "--json-root-name", "myOrcData",
            "--uri-template", "/orc/{/myOrcData/LastName}.json"
        );

        JsonNode doc = readJsonDocument("/orc/Humbee.json");
        assertEquals("Aida", doc.get("myOrcData").get("ForeName").asText());
    }

    @Test
    void orcFileWithCompressionTest() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "compression-test",
            "--uri-template", "/orc-compressed-test{LastName}.json",
            "-Pcompression=snappy"
        );

        getUrisInCollection("compression-test", 15).forEach(this::verifyDocContent);
    }

    @Test
    void badConfigurationItem() {
        String stderr = runAndReturnStderr(() ->
            run(
                "import-orc-files",
                "--path", "src/test/resources/orc-files",
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
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--path", "src/test/resources/avro/colors.avro",
            "--connection-string", makeConnectionString(),
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
            "import-parquet-files",
            "--path", "src/test/resources/avro/colors.avro",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
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
