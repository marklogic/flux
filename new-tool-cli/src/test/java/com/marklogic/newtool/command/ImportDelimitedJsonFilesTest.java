package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportDelimitedJsonFilesTest extends AbstractTest {

    @Test
    void defaultSettings() {
        run(
            "import_json_lines_files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--uriTemplate", "/delimited/{lastName}.json"
        );

        assertCollectionSize("delimited-json-test", 3);
        verifyDoc("/delimited/lastName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/delimited/lastName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/delimited/lastName-3.json", "firstName-3", "lastName-3");
    }

    @Test
    void jsonRootName() {
        run(
            "import_json_lines_files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--jsonRootName", "myData",
            "--uriTemplate", "/delimited/{/myData/lastName}.json"
        );

        JsonNode doc = readJsonDocument("/delimited/lastName-1.json");
        assertEquals("firstName-1", doc.get("myData").get("firstName").asText());
    }

    @Test
    void customDelimiter() {
        run(
            "import_json_lines_files",
            "--path", "src/test/resources/delimited-files/custom-delimiter-json.txt",
            "-PlineSep=:\n",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "custom-delimited-test",
            "--uriTemplate", "/custom/delimited/{firstName}.json"
        );

        assertCollectionSize("custom-delimited-test", 3);
        verifyDoc("/custom/delimited/firstName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/custom/delimited/firstName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/custom/delimited/firstName-3.json", "firstName-3", "lastName-3");
    }

    private void verifyDoc(String uri, String firstName, String lastName) {
        JsonNode doc = readJsonDocument(uri);
        assertEquals(firstName, doc.get("firstName").asText());
        assertEquals(lastName, doc.get("lastName").asText());
    }
}
