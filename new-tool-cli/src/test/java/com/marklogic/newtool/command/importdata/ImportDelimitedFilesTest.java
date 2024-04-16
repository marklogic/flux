package com.marklogic.newtool.command.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportDelimitedFilesTest extends AbstractTest {

    @Test
    void defaultSettings() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-test",
            "--uriTemplate", "/delimited/{number}.json"
        );

        assertCollectionSize("delimited-test", 3);
        verifyDoc("/delimited/1.json", 1, "blue", true);
        verifyDoc("/delimited/2.json", 2, "red", false);
        verifyDoc("/delimited/3.json", 3, "green", true);
    }

    @Test
    void jsonRootName() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--jsonRootName", "myData",
            "--uriTemplate", "/delimited/{/myData/number}.json"
        );

        JsonNode doc = readJsonDocument("/delimited/1.json");
        assertEquals("blue", doc.get("myData").get("color").asText());
    }

    @Test
    void gzip() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv.gz",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-test",
            "--uriTemplate", "/delimited/{number}.json"
        );

        assertCollectionSize("delimited-test", 3);
        verifyDoc("/delimited/1.json", 1, "blue", true);
        verifyDoc("/delimited/2.json", 2, "red", false);
        verifyDoc("/delimited/3.json", 3, "green", true);
    }

    @Test
    void customDelimiter() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/semicolon-delimiter.csv",
            "-Psep=;",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-test",
            "--uriTemplate", "/delimited/{number}.json"
        );

        assertCollectionSize("delimited-test", 3);
        verifyDoc("/delimited/1.json", 1, "blue", true);
        verifyDoc("/delimited/2.json", 2, "red", false);
        verifyDoc("/delimited/3.json", 3, "green", true);
    }

    @Test
    void noHeader() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/no-header.csv",
            "-Pheader=false",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "no-header",
            "--uriTemplate", "/no-header/{_c0}.json"
        );

        assertCollectionSize("no-header", 2);
        JsonNode doc = readJsonDocument("/no-header/1.json");
        assertEquals(1, doc.get("_c0").asInt(), "If no header row is present, Spark will name columns starting " +
            "with '_c0'.");
        assertEquals("blue", doc.get("_c1").asText());
        doc = readJsonDocument("/no-header/2.json");
        assertEquals(2, doc.get("_c0").asInt());
        assertEquals("red", doc.get("_c1").asText());
    }

    @Test
    void dontInferSchema() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "-PinferSchema=false",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "no-schema-inference",
            "--uriTemplate", "/delimited/{number}.json"
        );

        assertCollectionSize("no-schema-inference", 3);

        // Each field should be of type string since a schema was not inferred nor provided.
        JsonNode doc = readJsonDocument("/delimited/1.json");
        assertEquals(JsonNodeType.STRING, doc.get("number").getNodeType());
        assertEquals(JsonNodeType.STRING, doc.get("color").getNodeType());
        assertEquals(JsonNodeType.STRING, doc.get("flag").getNodeType());
    }

    /**
     * We expect limit and preview to work for every command; this is just a simple sanity check
     * for this command.
     */
    @Test
    @Disabled("stdout isn't being captured correctly for this test, will debug soon.")
    void limitAndPreview() {
        String stdout = runAndReturnStdout(() -> {
            run(
                "import_delimited_files",
                "--path", "src/test/resources/delimited-files/three-rows.csv",
                "--limit", "1",
                "--preview", "3",
                "--previewVertical"
            );
        });

        String message = "Unexpected stdout: " + stdout;
        assertTrue(stdout.contains("number | 1"), message);
        assertFalse(stdout.contains("number | 2"), message);
    }

    @Test
    void dontAbortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-test"
        ));

        // Must use this technique to get URIs, as the helper method in marklogic-junit fetches documents and the
        // documents have some encoding issues in them.
        QueryManager queryManager = getDatabaseClient().newQueryManager();
        StructuredQueryDefinition query = queryManager.newStructuredQueryBuilder().collection("delimited-test");
        queryManager.setView(QueryManager.QueryView.METADATA);
        JsonNode results = queryManager.search(query, new JacksonHandle()).get();
        int uriCount = results.get("total").asInt();
        assertTrue(
            uriCount > 0,
            "Spark CSV is very liberal in terms of what it allows. It will extract lines from " +
            "binary files like single-xml.zip. What's not clear is how Spark chooses a schema when it gets a " +
            "non-delimited file like single-xml.zip. In this test, it consistently chooses single-xml.zip to use " +
            "as the source of a schema, which results in a gibberish column name and only one row being created. " +
            "It's not clear if this process is indeterminate and thus Spark CSV may choose three-rows.csv sometimes " +
            "too. So we expect at least one URI to be written, as the command should default to not failing. " +
            "Actual URI count: " + uriCount
        );

        assertFalse(stderr.contains("Command failed"), "The command should not have failed because it defaults to " +
            "mode=DROPMALFORMED (mode=PERMISSIVE would also result in the command not failing); stderr: " + stderr);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--abortOnReadFailure",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-test"
        ));

        assertCollectionSize("delimited-test", 0);
        assertTrue(stderr.contains("Command failed, cause: [MALFORMED_RECORD_IN_PARSING]"), "The command should " +
            "have failed due to --abortOnReadFailure being included. This should result in the 'mode' option being " +
            "set to FAILFAST.");
    }

    private void verifyDoc(String uri, int expectedNumber, String expectedColor, boolean expectedFlag) {
        JsonNode doc = readJsonDocument(uri);

        assertEquals(expectedNumber, doc.get("number").asInt());
        assertEquals(JsonNodeType.NUMBER, doc.get("number").getNodeType());

        assertEquals(expectedColor, doc.get("color").asText());
        assertEquals(JsonNodeType.STRING, doc.get("color").getNodeType());

        assertEquals(expectedFlag, doc.get("flag").asBoolean());
        assertEquals(JsonNodeType.BOOLEAN, doc.get("flag").getNodeType());
    }
}
