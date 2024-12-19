/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ImportAggregateJsonFilesTest extends AbstractTest {

    /**
     * Verifies that both files containing a single object and files containing an array of objects can be read, with
     * each object becoming a document in MarkLogic.
     */
    @Test
    void objectFilesAndArrayOfObjectsFile() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates",
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
    void splitterSmokeTest() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-objects",
            "--uri-template", "/json-object/{number}.json",
            "--splitter-json-pointer", "/description",
            "--splitter-max-chunk-size", "30"
        );

        JsonNode doc = readJsonDocument("/json-object/2.json");
        assertEquals(2, doc.get("chunks").size());
    }

    @Test
    void uriIncludeFilePath() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "array-objects",
            "--uri-include-file-path",
            "--uri-replace", ".*resources,''"
        );

        getUrisInCollection("array-objects", 2).forEach(uri -> {
            assertTrue(uri.startsWith("/json-files/aggregates/array-of-objects.json/"), "Actual URI: " + uri);
            assertTrue(uri.endsWith(".json"), "Actual URI: " + uri);
        });
    }

    @Test
    void arrayOfObjects() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "array-objects",
            "--uri-template", "/array-object/{number}.json"
        );

        assertCollectionSize("array-objects", 2);
        JsonNode doc = readJsonDocument("/array-object/1.json");
        assertEquals("world", doc.get("hello").asText());
        doc = readJsonDocument("/array-object/2.json");
        assertEquals("This is different from the first object.", doc.get("description").asText());
    }

    @Test
    void arrayOfObjectsAsXml() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "array-objects",
            "--xml-root-name", "test",
            "--uri-template", "/array-object/{number}.xml"
        );

        assertCollectionSize("array-objects", 2);
        XmlNode doc = readXmlDocument("/array-object/1.xml");
        doc.assertElementValue("/test/hello", "world");
        doc = readXmlDocument("/array-object/2.xml");
        doc.assertElementValue("/test/description", "This is different from the first object.");
    }

    @Test
    void jsonLines() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--uri-template", "/delimited/{lastName}.json",

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "2"
        );

        assertCollectionSize("delimited-json-test", 3);
        verifyDoc("/delimited/lastName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/delimited/lastName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/delimited/lastName-3.json", "firstName-3", "lastName-3");
    }

    @Test
    void gzippedJsonLines() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt.gz",
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test",
            "--uri-template", "/delimited/{lastName}.json"
        );

        assertCollectionSize(
            "Spark data sources will automatically handle .gz files without -Pcompression=gzip being specified.",
            "delimited-json-test", 3
        );
        verifyDoc("/delimited/lastName-1.json", "firstName-1", "lastName-1");
        verifyDoc("/delimited/lastName-2.json", "firstName-2", "lastName-2");
        verifyDoc("/delimited/lastName-3.json", "firstName-3", "lastName-3");
    }


    @Test
    void jsonRootName() {
        run(
            "import-aggregate-json-files",
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
            "import-aggregate-json-files",
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
     * This documents that if a user has a ZIP file, and we would normally use a Spark data source to read the type files
     * inside the ZIP, we unfortunately cannot do that as Spark doesn't have any support for ZIP files. Databricks
     * documentation - https://docs.databricks.com/en/files/unzip-files.html - confirms this, noting that if a user has
     * a ZIP file, they should first expand it.
     * <p>
     * So for ZIP files, the best we can do is use our own reader, which is limited to reading each file as a "file row"
     * and then writing it as a document to MarkLogic. Which means that a user cannot use a feature like
     * "--uri-template", as that depends on having values in columns that can be referenced by the template. We will
     * hopefully be enhancing this in a future story - specifically, by enhancing the URI template feature to work on
     * file rows and document rows.
     */
    @Test
    void zipOfJsonObjectFiles() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/aggregates/object-files/objects.zip",
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
            "import-aggregate-json-files",
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
            "import-aggregate-json-files",
            "--path", "src/test/resources/delimited-files/line-delimited-json.txt",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--json-lines",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-json-test"
        ));

        assertCollectionSize("delimited-json-test", 0);
        assertTrue(stderr.contains("Error: Invalid UTF-8 start"), "The command should have failed " +
            "due to the invalid single-xml.zip file being included along with --abort-on-read-failure being " +
            "included as well; actual stderr: " + stderr);
    }

    @Test
    void ignoreNullFields() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/json-files/aggregates",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-objects",
            "--uri-template", "/json-object/{number}.json",
            "--filter", "*.json",
            "--ignore-null-fields"
        );

        JsonNode doc = readJsonDocument("/json-object/1.json");
        assertEquals(1, doc.get("number").asInt());
        assertEquals("world", doc.get("hello").asText());
        assertFalse(doc.has("description"), "The description column, which has a null value for this row, " +
            "should not exist due to the use of --ignore-null-fields.");
    }

    /**
     * Verifies that the MarkLogic connector is used instead of Spark JSON, as the latter enforces a schema across all
     * rows and thus can reorder keys and add fields when not desired. The MarkLogic connector reads each line as-is.
     */
    @Test
    void readJsonLinesRaw() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/delimited-files/different-objects.txt",
            "--json-lines-raw",
            // This is included only to ensure it does not cause an error. It does not have any impact as it only
            // matters when the Spark JSON data source is used.
            "--uri-include-file-path",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-lines",
            "--uri-template", "/a/{id}.json"
        );

        assertCollectionSize("json-lines", 2);

        JsonNode doc = readJsonDocument("/a/1.json");
        assertEquals(1, doc.get("id").asInt());
        assertEquals(2, doc.size(), "The doc should not have the 'color' field from the second line.");
        Iterator<String> names = doc.fieldNames();
        assertEquals("id", names.next(), "The JSON object should not be modified in any way when " +
            "--json-lines-raw is used, so the 'id' key should still be first.");
        assertEquals("hello", names.next());

        doc = readJsonDocument("/a/2.json");
        assertEquals(2, doc.get("id").asInt());
        assertEquals(2, doc.size(), "The doc should not have the 'hello' field from the first line.");
        names = doc.fieldNames();
        assertEquals("id", names.next());
        assertEquals("color", names.next());
    }

    @Test
    void readJsonLinesRawGzipped() {
        run(
            "import-aggregate-json-files",
            "--path", "src/test/resources/delimited-files/json-lines.gz",
            "--json-lines-raw",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-lines",
            "--uri-template", "/a/{id}.json"
        );

        assertCollectionSize("To be as similar to Spark JSON as possible, the MarkLogic connector will assume that a " +
            ".gz or .gzip file is gzipped, and thus the user does not need a --compression option.", "json-lines", 2);
    }

    private void verifyDoc(String uri, String firstName, String lastName) {
        JsonNode doc = readJsonDocument(uri);
        assertEquals(firstName, doc.get("firstName").asText());
        assertEquals(lastName, doc.get("lastName").asText());
    }
}
