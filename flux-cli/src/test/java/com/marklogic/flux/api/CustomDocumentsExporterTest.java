/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void noQuerySpecified() {
        CustomDocumentsExporter exporter = Flux.customExportDocuments()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify at least one of the following for the documents to export: " +
                "collections; a directory; a string query; a structured, serialized, or combined query; or URIs.",
            ex.getMessage());
    }
}
