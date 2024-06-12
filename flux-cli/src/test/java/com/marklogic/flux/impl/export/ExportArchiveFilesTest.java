package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportArchiveFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export-archive-files",
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--file-count", "1",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".zip"), "Unexpected filename: " + files[0].getName());

        // Import the file back in to verify its contents.
        run(
            "import-archive-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--collections", "imported-author",
            "--uri-prefix", "/imported"
        );

        assertCollectionSize("Being able to read these URIs verifies that the metadata was exported and imported " +
            "correctly, as without any document permissions from the metadata files, we wouldn't have been able " +
            "to write nor read these documents.", "imported-author", 15);

        assertCollectionSize("The original 'author' collection should not have been affected since we " +
            "specified a different collection for the import.", "author", 15);
    }

    @Test
    void contentShouldAlwaysBeIncluded(@TempDir Path tempDir) {
        String stderr = runAndReturnStderr(() -> run(
            "export-archive-files",
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--categories", "collections,permissions"
        ));

        assertFalse(stderr.contains("Command failed"), "Unexpected command failure: " + stderr);

        // Import the file back in to verify its contents.
        run(
            "import-archive-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--collections", "imported-author",
            "--uri-prefix", "/imported"
        );

        assertCollectionSize("The export command should always include content, even when --categories is used " +
            "to select a subset of metadata. Thus, the import should work as well and reuse the permissions that " +
            "were retrieved in the original import.", "imported-author", 15);
    }
}
