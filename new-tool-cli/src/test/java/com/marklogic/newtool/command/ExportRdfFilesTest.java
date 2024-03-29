package com.marklogic.newtool.command;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class ExportRdfFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        // Load some triples to export first.
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples"
        );

        // Now export them.
        run(
            "export_rdf_files",
            "--clientUri", makeClientUri(),
            "--collections", "my-triples",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--format", "nq",
            "--graph", "my-graph"
        );

        // And import them again to make sure we get the expected amount, but in a different collection.
        run(
            "import_rdf_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "more-triples"
        );

        String triplesUri = getUrisInCollection("more-triples", 1).get(0);
        XmlNode doc = readXmlDocument(triplesUri);
        doc.assertElementCount("The original englishlocale.ttl file has 32 triples in it. So after importing that, " +
                "and then exporting it to 1 or more files, and then importing thos files, we still expect to have 32 " +
                "triples, just in a different collection via the second import.",
            "/sem:triples/sem:triple", 32
        );
    }
}
