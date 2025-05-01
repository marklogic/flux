/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RdfFilesImporterTest extends AbstractTest {

    private static final String DEFAULT_MARKLOGIC_GRAPH = "http://marklogic.com/semantics#default-graph";

    @Test
    void withGraph() {
        Flux.importRdfFiles()
            .from("src/test/resources/rdf/englishlocale.ttl")
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .graph("my-graph"))
            .execute();

        assertCollectionSize("my-graph", 2);
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
    }

    @Test
    void withGraphOverride() {
        Flux.importRdfFiles()
            .from("src/test/resources/rdf/three-quads.trig")
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .graphOverride("my-other-graph"))
            .execute();

        assertCollectionSize("my-other-graph", 2);

        // Make sure nothing got written to any of the other possible graphs.
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G1", 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G2", 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G3", 0);
    }

    @Test
    void gzippedFile() {
        Flux.importRdfFiles()
            .from(options -> options
                .paths("src/test/resources/rdf/englishlocale2.ttl.gz")
                .compressionType(CompressionType.GZIP))
            .connectionString(makeConnectionString())
            .to(options -> options.permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 2);
    }

    @Test
    void missingPath() {
        RdfFilesImporter importer = Flux.importRdfFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
