package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class RdfFilesImporterTest extends AbstractTest {

    private static final String DEFAULT_MARKLOGIC_GRAPH = "http://marklogic.com/semantics#default-graph";

    @Test
    void withGraph() {
        NT.importRdfFiles()
            .readFiles("src/test/resources/rdf/englishlocale.ttl")
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .graph("my-graph"))
            .execute();

        assertCollectionSize("my-graph", 2);
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
    }

    @Test
    void withGraphOverride() {
        NT.importRdfFiles()
            .readFiles("src/test/resources/rdf/three-quads.trig")
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options
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
        NT.importRdfFiles()
            .readFiles(options -> options
                .paths("src/test/resources/rdf/englishlocale2.ttl.gz")
                .compressionType(CompressionType.GZIP))
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options.permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 2);
    }

}
