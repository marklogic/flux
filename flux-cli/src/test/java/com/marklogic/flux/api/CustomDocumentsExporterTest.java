/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDocumentsExporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.customExportDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author"))
            .to(options -> options
                .target("marklogic")
                .additionalOptions(Map.of(
                    Options.CLIENT_URI, makeConnectionString(),
                    Options.WRITE_PERMISSIONS, DEFAULT_PERMISSIONS,
                    Options.WRITE_URI_PREFIX, "/exported",
                    Options.WRITE_COLLECTIONS, "exported-authors"
                )))
            .execute();

        getUrisInCollection("exported-authors", 15)
            .forEach(uri -> assertTrue(uri.startsWith("/exported/author/"), "Unexpected URI: " + uri));
    }
}
