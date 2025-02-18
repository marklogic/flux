/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtractTextTest extends AbstractTest {

    @Test
    void test() {
        final String collection = "extraction-test";

        run(
            "import-files",
            "--path", "src/test/resources/extraction-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", collection,
            "--uri-replace", ".*/resources,''",
            "--extract-text", "--stacktrace",
            "--extracted-text-collection", collection
        );

        assertCollectionSize("Should have the 2 binary docs and the 2 docs with extracted text", collection, 4);
        assertInCollections("/extraction-files/hello-world.docx-extracted-text.json", collection);
        assertInCollections("/extraction-files/marklogic-getting-started.pdf-extracted-text.json", collection);
    }

    /**
     * Intent is to make sure that the org.apache.tika:tika-parser-microsoft-module dependency isn't running into any
     * classpath conflicts with Spark or another 3rd party library. Have had issues with both commons-compress and
     * org.apache.logging.log4j dependencies.
     */
    @Test
    void microsoftFile() {
        final String collection = "extraction-test";

        run(
            "import-files",
            "--path", "src/test/resources/extraction-files",
            "--filter", "*.docx",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", collection,
            "--uri-replace", ".*/resources,''",
            "--extract-text", "--stacktrace",
            "--extracted-text-collection", collection
        );

        assertCollectionSize(collection, 2);
        JsonNode doc = readJsonDocument("/extraction-files/hello-world.docx-extracted-text.json", collection);
        assertEquals("/extraction-files/hello-world.docx", doc.get("source-uri").asText());
        assertEquals("Hello world.\n\nThis file is used for testing text extraction.\n", doc.get("content").asText());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            doc.get("metadata").get("Content-Type").asText());
    }
}
