package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RdfFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.importRdfFiles()
            .readFiles("src/test/resources/rdf/englishlocale.ttl")
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options.permissionsString(DEFAULT_PERMISSIONS).graph("my-graph"))
            .execute();

        NT.exportRdfFiles()
            .connectionString(makeConnectionString())
            .readTriples(options -> options.graphs("my-graph"))
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .format("nq")
                .graphOverride("my-new-graph"))
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);

        NT.importRdfFiles()
            .readFiles(tempDir.toFile().getAbsolutePath())
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options.permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("The collection should have 2 documents - one containing the " +
            "triples, and then the semantic graph document.", "my-new-graph", 2);
    }
}
