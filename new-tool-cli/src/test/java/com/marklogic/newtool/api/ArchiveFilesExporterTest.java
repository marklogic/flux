package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchiveFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .readDocuments(options -> options.collections("author").categories("content", "collections", "permissions"))
            .writeFiles(options -> options.path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
            )
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".zip"), "Unexpected filename: " + files[0].getName());

        // Import the file back in to verify its contents.
        NT.importArchiveFiles()
            .readFiles(tempDir.toFile().getAbsolutePath())
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options.collections("imported-author").uriPrefix("/imported"))
            .execute();

        assertCollectionSize("Being able to read these URIs verifies that the metadata was exported and imported " +
            "correctly, as without any document permissions from the metadata files, we wouldn't have been able " +
            "to write nor read these documents.", "imported-author", 15);

    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        NT.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .readDocuments(options -> options.collections("author").categories("content", "collections", "permissions"))
            .limit(1)
            .writeFiles(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
    }

    @Test
    void missingPath() {
        ArchiveFilesExporter exporter = NT.exportArchiveFiles()
            .connectionString(makeConnectionString())
            .readDocuments(options -> options.collections("author").categories("content", "collections", "permissions"));

        NtException ex = assertThrowsNtException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
