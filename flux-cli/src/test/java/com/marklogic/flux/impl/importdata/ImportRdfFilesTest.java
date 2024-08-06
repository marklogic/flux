/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportRdfFilesTest extends AbstractTest {

    private static final String DEFAULT_MARKLOGIC_GRAPH = "http://marklogic.com/semantics#default-graph";

    @Test
    void noGraph() {
        run(
            "import-rdf-files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples",

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "1"
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
            "import-rdf-files",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--graph", "my-graph"
        );

        assertCollectionSize("my-graph", 2);
        assertCollectionSize(DEFAULT_MARKLOGIC_GRAPH, 0);
    }

    @Test
    void withGraphOverride() {
        run(
            "import-rdf-files",
            "--path", "src/test/resources/rdf/three-quads.trig",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--graph-override", "my-other-graph"
        );

        assertCollectionSize(
            "All the quads in three-quads.trig should be added to the same managed triples document; their graphs " +
                "should be ignored in favor of the --graph-override value.", "my-other-graph", 2
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
            "import-rdf-files",
            "--path", "src/test/resources/rdf/englishlocale2.ttl.gz",
            "--connection-string", makeConnectionString(),
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
            "import-rdf-files",
            "--path", "src/test/resources/rdf/each-rdf-file-type.zip",
            "--connection-string", makeConnectionString(),
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
    void invalidFileDontAbort() {
        run(
            "import-rdf-files",
            "--path", "src/test/resources/mixed-files/hello2.txt.gz",
            "--path", "src/test/resources/rdf/englishlocale.ttl",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples"
        );

        assertCollectionSize("An error should have been logged for the non-RDF hello2.txt.gz file but it should not " +
            "have caused the command to fail, which defaults to not aborting on read failure.", "my-triples", 1);
    }

    @Test
    void invalidFileAbort() {
        String stderr = runAndReturnStderr(() -> run(
            "import-rdf-files",
            "--path", "src/test/resources/mixed-files/hello2.txt.gz",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
            "--collections", "my-triples"
        ));

        assertTrue(stderr.contains("Command failed, cause: Unable to read file at"),
            "Unexpected stderr: " + stderr);
        assertCollectionSize("my-triples", 0);
    }
}
