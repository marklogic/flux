/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportRdfFilesTest extends AbstractTest {

    @Test
    void importThenExportThenImport(@TempDir Path tempDir) {
        run(
            "import-rdf-files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples"
        );

        run(
            "export-rdf-files",
            "--connection-string", makeConnectionString(),
            "--graphs", "my-triples",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--format", "nq",
            "--file-count", "1",
            "--batch-size", "5",
            "--log-progress", "10"
        );

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".nq"), "Unexpected filename: " + files[0].getName());

        // And import them again to make sure we get the expected amount, but in a different collection.
        run(
            "import-rdf-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "more-triples"
        );

        String triplesUri = getUrisInCollection("more-triples", 1).get(0);
        XmlNode doc = readXmlDocument(triplesUri);
        doc.assertElementCount("The original englishlocale.ttl file has 32 triples in it. So after importing that, " +
                "and then exporting it to 1 or more files, and then importing those files, we still expect to have 32 " +
                "triples, just in a different collection via the second import.",
            "/sem:triples/sem:triple", 32
        );
    }

    @Test
    void missingQueryInput(@TempDir Path tempDir) {
        String stderr = runAndReturnStderr(() -> run(
            "export-rdf-files",
            "--connection-string", makeConnectionString(),
            "--path", tempDir.toFile().getAbsolutePath()
        ));

        assertTrue(stderr.contains("Must specify at least one of the following options: " +
                "[--collections, --directory, --graphs, --query, --string-query, --uris]."),
            "Unexpected stderr: " + stderr);
    }
}
