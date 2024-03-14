package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
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
