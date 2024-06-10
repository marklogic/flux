package com.marklogic.newtool.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportJsonFilesTest extends AbstractTest {

    /**
     * Verifies that both files containing a single object and files containing an array of objects can be read, with
     * each object becoming a document in MarkLogic.
     */
    @Test
    void objectFilesAndArrayOfObjectsFile() {
        run(
            "import-json-files",
            "--path", "src/test/resources/json-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-objects",
            "--uri-template", "/json-object/{number}.json",
            "--filter", "*.json"
        );

        assertCollectionSize("json-objects", 4);

        JsonNode doc = readJsonDocument("/json-object/1.json");
        assertEquals(1, doc.get("number").asInt());
        assertEquals("world", doc.get("hello").asText());

        doc = readJsonDocument("/json-object/2.json");
        assertEquals(2, doc.get("number").asInt());
        assertEquals("This is different from the first object.", doc.get("description").asText());

        doc = readJsonDocument("/json-object/3.json");
        assertEquals(3, doc.get("number").asInt());
        assertEquals("object 3", doc.get("hello").asText());

        doc = readJsonDocument("/json-object/4.json");
        assertEquals(4, doc.get("number").asInt());
        assertEquals("object 4", doc.get("hello").asText());
    }

    @Test
    void jsonLines() {
        run(
            "import-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--uri-template", "/delimited/{lastName}.json"
        );

        assertCollectionSize("delimited-json-test", 3);
        verifyDoc("/delimited/lastName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/delimited/lastName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/delimited/lastName-3.json", "firstName-3", "lastName-3");
    }

    @Test
    void jsonRootName() {
        run(
            "import-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--json-root-name", "myData",
            "--uri-template", "/delimited/{/myData/lastName}.json"
        );

        JsonNode doc = readJsonDocument("/delimited/lastName-1.json");
        assertEquals("firstName-1", doc.get("myData").get("firstName").asText());
    }

    @Test
    void jsonLinesWithCustomDelimiter() {
        run(
            "import-json-files",
            "--path", "src/test/resources/delimited-files/custom-delimiter-json.txt",
            "--json-lines",
            "-PlineSep=:\n",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "custom-delimited-test",
            "--uri-template", "/custom/delimited/{firstName}.json"
        );

        assertCollectionSize("custom-delimited-test", 3);
        verifyDoc("/custom/delimited/firstName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/custom/delimited/firstName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/custom/delimited/firstName-3.json", "firstName-3", "lastName-3");
    }

    /**
     * This documents that if a user has a zip file, and we would normally use a Spark data source to read the type files
     * inside the zip, we unfortunately cannot do that as Spark doesn't have any support for zip files. Databricks
     * documentation - https://docs.databricks.com/en/files/unzip-files.html - confirms this, noting that if a user has
     * a zip file, they should first expand it.
     * <p>
     * So for zip files, the best we can do is use our own reader, which is limited to reading each file as a "file row"
     * and then writing it as a document to MarkLogic. Which means that a user cannot use a feature like
     * "--uri-template", as that depends on having values in columns that can be referenced by the template. We will
     * hopefully be enhancing this in a future story - specifically, by enhancing the URI template feature to work on
     * file rows and document rows.
     */
    @Test
    void zipOfJsonObjectFiles() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/object-files/objects.zip",
            "--compression", "zip",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "zipped-objects",
            "--uri-replace", ".*object-files,''"
        );

        JsonNode doc = readJsonDocument("/objects.zip/object3.json");
        assertEquals(3, doc.get("number").asInt());
        doc = readJsonDocument("/objects.zip/object4.json");
        assertEquals(4, doc.get("number").asInt());
    }

    @Test
    void dontAbortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test"
        ));

        assertCollectionSize(
            "The 3 valid lines in line-delimited-json.txt should be processed, with the data in single-xml.zip " +
                "being ignored due to the command defaulting to mode=DROPMALFORMED.",
            "delimited-json-test", 3
        );

        assertFalse(stderr.contains("Command failed"), "The command should default to mode=DROPMALFORMED so that " +
            "invalid lines do not produce an error but rather are discarded.");
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--json-lines",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test"
        ));

        assertCollectionSize("delimited-json-test", 0);
        assertTrue(stderr.contains("Command failed, cause: Invalid UTF-8 start"), "The command should have failed " +
            "due to the invalid single-xml.zip file being included along with --abort-on-read-failure being " +
            "included as well; actual stderr: " + stderr);
    }

    private void verifyDoc(String uri, String firstName, String lastName) {
        JsonNode doc = readJsonDocument(uri);
        assertEquals(firstName, doc.get("firstName").asText());
        assertEquals(lastName, doc.get("lastName").asText());
    }
}
