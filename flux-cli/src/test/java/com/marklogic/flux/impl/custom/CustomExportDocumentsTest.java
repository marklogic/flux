/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomExportDocumentsTest extends AbstractTest {

    @Test
    void test() {
        run(
            "custom-export-documents",
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--target", "marklogic",
            "--spark-prop", String.format("%s=%s", Options.CLIENT_URI, makeConnectionString()),
            "--spark-prop", String.format("%s=%s", Options.WRITE_PERMISSIONS, DEFAULT_PERMISSIONS),
            "--spark-prop", String.format("%s=/exported", Options.WRITE_URI_PREFIX),
            "--spark-prop", String.format("%s=exported-authors", Options.WRITE_COLLECTIONS)
        );

        assertCollectionSize("We can use our own connector as a custom target, and thus we expect the 15 " +
                "author documents to have been written with different URIs to the 'exported-authors' collection.",
            "exported-authors", 15);

        getUrisInCollection("exported-authors", 15)
            .forEach(uri -> assertTrue(uri.startsWith("/exported/author/"), "Unexpected URI: " + uri));
    }
}
