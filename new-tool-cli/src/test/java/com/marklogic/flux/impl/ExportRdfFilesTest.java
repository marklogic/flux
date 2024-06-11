package com.marklogic.flux.impl;

import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
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
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples"
        );

        run(
            "export_rdf_files",
            "--connectionString", makeConnectionString(),
            "--graphs", "my-triples",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--format", "nq",
            "--fileCount", "1"
        );

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".nq"), "Unexpected filename: " + files[0].getName());

        // And import them again to make sure we get the expected amount, but in a different collection.
        run(
            "import_rdf_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connectionString", makeConnectionString(),
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
            "export_rdf_files",
            "--connectionString", makeConnectionString(),
            "--path", tempDir.toFile().getAbsolutePath()
        ));

        assertTrue(stderr.contains("Must specify at least one of the following options: " +
                "[--graphs, --query, --uris, --stringQuery, --collections, --directory]."),
            "Unexpected stderr: " + stderr);
    }
}
