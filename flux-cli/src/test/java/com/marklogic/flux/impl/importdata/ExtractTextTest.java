/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class ExtractTextTest extends AbstractTest {

    @Test
    void test() {
        final String collection = "extraction-test";

        run(
            "import-files",
            "--path", "src/test/resources/extraction-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", collection,
            "--uri-replace", ".*/resources,''",
            "--extract-text"
        );

        assertCollectionSize("Should have the 2 binary docs and the 2 docs with extracted text", collection, 4);
        assertInCollections("/extraction-files/hello-world.docx-extracted-text.txt", collection);
        assertInCollections("/extraction-files/marklogic-getting-started.pdf-extracted-text.txt", collection);
    }
}
