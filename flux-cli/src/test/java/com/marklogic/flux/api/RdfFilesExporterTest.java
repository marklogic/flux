/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RdfFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.importRdfFiles()
            .from("src/test/resources/rdf/englishlocale.ttl")
            .connectionString(makeConnectionString())
            .to(options -> options.permissionsString(DEFAULT_PERMISSIONS).graph("my-graph"))
            .execute();

        Flux.exportRdfFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.graphs("my-graph"))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .format("nq")
                .graphOverride("my-new-graph"))
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);

        Flux.importRdfFiles()
            .from(tempDir.toFile().getAbsolutePath())
            .connectionString(makeConnectionString())
            .to(options -> options.permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("The collection should have 2 documents - one containing the " +
            "triples, and then the semantic graph document.", "my-new-graph", 2);
    }

    @Test
    void gzip(@TempDir Path tempDir) {
        Flux.importRdfFiles()
            .from("src/test/resources/rdf/englishlocale.ttl")
            .connectionString(makeConnectionString())
            .to(options -> options.permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        Flux.exportRdfFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.graphs("http://marklogic.com/semantics#default-graph"))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .format("nq")
                .gzip())
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".nq.gz"));
    }

    @Test
    void noQuerySpecified() {
        RdfFilesExporter exporter = Flux.exportRdfFiles()
            .connectionString(makeConnectionString())
            .to(options -> options.path("build/doesnt-matter"));

        FluxException ex = assertThrowsFluxException(exporter::execute);
        assertEquals("Must specify at least one of the following for the triples to export: " +
                "collections; a directory; graphs; a string query; a structured, serialized, or combined query; or URIs.",
            ex.getMessage());
    }
}
