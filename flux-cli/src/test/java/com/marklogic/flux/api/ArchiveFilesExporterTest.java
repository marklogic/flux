/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchiveFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content", "collections", "permissions"))
            .to(options -> options.path(tempDir.toFile().getAbsolutePath()).fileCount(1))
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".zip"), "Unexpected filename: " + files[0].getName());

        // Import the file back in to verify its contents.
        Flux.importArchiveFiles()
            .from(tempDir.toFile().getAbsolutePath())
            .connectionString(makeConnectionString())
            .to(options -> options.collections("imported-author").uriPrefix("/imported"))
            .execute();

        assertCollectionSize("Being able to read these URIs verifies that the metadata was exported and imported " +
            "correctly, as without any document permissions from the metadata files, we wouldn't have been able " +
            "to write nor read these documents.", "imported-author", 15);

    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        Flux.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content", "collections", "permissions"))
            .limit(1)
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
    }

    @Test
    void missingPath() {
        ArchiveFilesExporter exporter = Flux.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content", "collections", "permissions"));

        FluxException ex = assertThrowsFluxException(exporter::execute);
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
