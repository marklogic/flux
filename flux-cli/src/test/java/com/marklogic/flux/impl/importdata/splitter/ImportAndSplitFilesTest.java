/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata.splitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAndSplitFilesTest extends AbstractTest {

    @Test
    void splitNamespacedXml() {
        run(
            "import-files",
            "--path", "src/test/resources/xml-file/namespaced-java-client-intro.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/xml-file,''",
            "--splitter-xml-xpath", "/ex:root/ex:text/text()",
            "--splitter-xml-namespace", "ex=org:example",
            "--splitter-max-chunk-size", "500",
            "--splitter-max-overlap-size", "100",
            "--stacktrace"
        );

        XmlNode doc = readXmlDocument("/namespaced-java-client-intro.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example")});
        doc.assertElementCount("The underlying langchain4j splitter is expected to produce 5 chunks when using a " +
            "max chunk size of 500 and a max overlap size of 100.", "/ex:root/chunks/chunk", 5);
    }

    /**
     * This isn't the greatest error message; picocli generates a message that reveals a little bit of
     * implementation detail by mentioning the Namespace class. But the message is good enough to point the user
     * to the problem.
     */
    @Test
    void invalidNamespaceDeclaration() {
        assertStderrContains(() -> run(
            "import-files",
            "--path", "src/test/resources/xml-file/namespaced-java-client-intro.xml",
            "--connection-string", makeConnectionString(),
            "--splitter-xml-xpath", "/ex:root/ex:text/text()",
            "--splitter-xml-namespace", "org:example"
        ), "Invalid value for option '--splitter-xml-namespace' (<xmlNamespaces>): cannot convert 'org:example' to Namespace " +
            "(com.marklogic.flux.api.FluxException: The value must match the pattern prefix=namespaceURI)");
    }

    @Test
    void splitWithMultipleJsonPointers() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-json-pointer", "/more-text",
            "--splitter-max-chunk-size", "500",
            "--stacktrace"
        );

        JsonNode doc = readJsonDocument("/java-client-intro.json");
        ArrayNode chunks = (ArrayNode) doc.get("chunks");
        assertEquals(4, chunks.size(), "Expecting 4 chunks based on the max chunk size of 500.");
        String lastChunk = chunks.get(3).get("text").asText();
        assertTrue(lastChunk.endsWith("Choose a REST API Instance. Hello world."), "The last chunk should end with " +
            "the text at the end of the '/text' path, plus a space, plus the text from the '/more-text/ path. " +
            "Actual chunk: " + lastChunk);
    }

    @Test
    void emptyJsonPointer() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", ""
        );

        JsonNode doc = readJsonDocument("/java-client-intro.json");
        ArrayNode chunks = (ArrayNode) doc.get("chunks");

        String firstChunk = chunks.get(0).get("text").asText();
        assertTrue(firstChunk.startsWith("{\"url\":\"https://docs"), "The JSON Pointer expression '\"\"' is valid " +
            "and refers to the entire document, which is then expected to be serialized into a string. So the first " +
            "chunk should start with the serialization of the document. Actual chunk: " + firstChunk);
    }

    @Test
    void splitWithRegexAndDelimiter() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/more-text",
            "--splitter-regex", "w",
            "--splitter-join-delimiter", "---"
        );

        JsonNode doc = readJsonDocument("/java-client-intro.json");
        ArrayNode chunks = (ArrayNode) doc.get("chunks");
        assertEquals(1, chunks.size());
        assertEquals("Hello ---orld.", chunks.get(0).get("text").asText(),
            "This of course isn't a realistic regex, but it verifies that the 'w' produces two chunks " +
                "that are then joined together with the given delimiter, as the resulting chunk length " +
                "is less than the max chunk size.");
    }

    @Test
    void customSplitter() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-custom-class", "com.marklogic.flux.impl.importdata.splitter.CustomSplitter",
            "--splitter-custom-option", "textToReturn=just testing"
        );

        ArrayNode chunks = (ArrayNode) readJsonDocument("/java-client-intro.json").get("chunks");
        assertEquals(1, chunks.size());
        assertEquals("just testing", chunks.get(0).get("text").asText(), "Verifying that the custom " +
            "splitter is used; it should return the text specified by the 'textToReturn' custom class option.");
    }

    @Test
    void invalidCustomSplitterClassName() {
        assertStderrContains(() -> run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--splitter-json-pointer", "/text",
            "--splitter-custom-class", "doesnt.exist.ClassName"
        ), "Command failed, cause: Cannot find custom splitter with class name: doesnt.exist.ClassName");
    }

    @Test
    void invalidCustomSplitterOption() {
        assertStderrContains(() -> run(
                "import-files",
                "--path", "src/test/resources/json-files/java-client-intro.json",
                "--connection-string", makeConnectionString(),
                "--permissions", DEFAULT_PERMISSIONS,
                "--splitter-json-pointer", "/text",
                "--splitter-custom-class", "com.marklogic.flux.impl.importdata.splitter.CustomSplitter",
                "--splitter-custom-option", "missing an equals"
            ),
            // This is the default picocli error message for an invalid map option. It's a little technical, but seems
            // reasonable enough for a user to be able to fix their mistake.
            "Value for option '--splitter-custom-option' (<String=String>) should be in KEY=VALUE format but was missing an equals"
        );
    }
}
