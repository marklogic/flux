/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractJava17Test;
import org.junit.jupiter.api.Test;

class ImportWithAllProcessingTest extends AbstractJava17Test {

    @Test
    void extractAndSplitAndEmbed() {
        run(
            "import-files",
            "--path", "../flux-cli/src/test/resources/extraction-files/marklogic-getting-started.pdf",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "binary-file",
            "--uri-replace", ".*/extraction-files,''",
            "--extract-text",
            "--extracted-text-collections", "extracted-text",
            "--extracted-text-permissions", DEFAULT_PERMISSIONS,
            "--splitter-text",
            "--splitter-sidecar-max-chunks", "1",
            "--splitter-max-chunk-size", "10000",
            "--splitter-sidecar-collections", "chunks",
            "--embedder", "minilm"
        );

        assertCollectionSize("binary-file", 1);
        assertInCollections("/marklogic-getting-started.pdf", "binary-file");

        assertCollectionSize("extracted-text", 1);
        assertInCollections("/marklogic-getting-started.pdf-extracted-text.json", "extracted-text");

        assertCollectionSize("Expecting 5 chunks based on the PDF size and the max chunk size", "chunks", 5);
    }
}
