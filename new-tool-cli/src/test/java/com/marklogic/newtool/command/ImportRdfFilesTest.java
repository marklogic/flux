package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportRdfFilesTest extends AbstractTest {

    private static final String DEFAULT_MARKLOGIC_GRAPH = "http://marklogic.com/semantics#default-graph";

    @Test
    void noGraph() {
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples"
        );

        assertCollectionSize(
            "The default MarkLogic graph collection should contain a sem:graph document and a single managed " +
                "triples document containing the imported triples.", DEFAULT_MARKLOGIC_GRAPH, 2
        );
        assertCollectionSize(
            "The managed triples document should be in the user-specified collection as well as the default " +
                "MarkLogic graph collection", "my-triples", 1
        );
    }

    @Test
    void withGraph() {
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--graph", "my-graph"
        );

        assertCollectionSize("my-graph", 2);
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
    }

    @Test
    void withGraphOverride() {
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/three-quads.trig",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--graphOverride", "my-other-graph"
        );

        assertCollectionSize(
            "All the quads in three-quads.trig should be added to the same managed triples document; their graphs " +
                "should be ignored in favor of the --graphOverride value.", "my-other-graph", 2
        );

        // Make sure nothing got written to any of the other possible graphs.
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G1", 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G2", 0);
        assertCollectionSize("http://www.example.org/exampleDocument#G3", 0);
    }

    @Test
    void gzippedFile() {
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale2.ttl.gz",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--compression", "gzip"
        );

        assertCollectionSize(
            "Just verifying that the --compression option works for 'gzip'",
            DEFAULT_MARKLOGIC_GRAPH, 2
        );
    }

    @Test
    void zipContainingEachFileType() {
        run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/each-rdf-file-type.zip",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "all-my-rdf",
            "--compression", "zip"
        );

        assertCollectionSize(
            "Should have 7 graph documents - the default MarkLogic one for all the triples files; 3 from the " +
                "three-quads.trig file; and 3 from the semantics.nq file.",
            "http://marklogic.com/semantics#graphs", 7
        );
        assertCollectionSize(
            "Should have 7 separate docs in the user-defined collection; all of the triples are in 1 document since " +
                "they're in the default MarkLogic graph; and then each of the 6 graphs across three-quads.trig and " +
                "semantics.nq should have their triples placed in separate documents so they can be in separate " +
                "graph collections",
            "all-my-rdf", 7
        );
    }

    @Test
    void uriTemplateOptionShouldNotWork() {
        String stderr = runAndReturnStderr(() -> run(
            "import_rdf_files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples",
            "--uriTemplate", "/test/{uri}"
        ));

        // We may want to eventually modify this JCommander error, as it's not intuitive as to what it means.
        assertTrue(stderr.contains("Was passed main parameter '--uriTemplate'"),
            "Was expecting an error due to --uriTemplate not being supported for importing RDF files, as it doesn't " +
                "have any meaningful use case since we default to '/triplestore/uuid.xml' for managed triples " +
                "documents. Unexpected stderr: " + stderr);
    }
}
