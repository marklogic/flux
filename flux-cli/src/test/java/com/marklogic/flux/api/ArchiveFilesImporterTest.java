/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchiveFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importArchiveFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/archive-files")
                .categories("content", "permissions"))
            .execute();

        Stream.of("/test/1.xml", "/test/2.xml").forEach(uri -> {
            DocumentMetadataHandle metadata = getDatabaseClient().newDocumentManager().readMetadata(uri, new DocumentMetadataHandle());
            assertEquals(0, metadata.getCollections().size(), "Since 'collections' was not included in the list " +
                "of categories, no collections should be assigned to URI: " + uri);
        });
    }

    @Test
    void missingPath() {
        ArchiveFilesImporter importer = Flux.importArchiveFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
