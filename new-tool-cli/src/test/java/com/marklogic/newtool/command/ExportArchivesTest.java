package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportArchivesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export_archives",
            "--clientUri", makeClientUri(),
            "--collections", "author",
            // May want a "--files" alias here.
            "--repartition", "1",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().endsWith(".zip"), "Unexpected filename: " + files[0].getName());

        // Import the file back in to verify its contents.
        run(
            "import_archives",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--clientUri", makeClientUri(),
            "--collections", "imported-author"
        );

        assertCollectionSize("Being able to read these URIs verifies that the metadata was exported and imported " +
            "correctly, as without any document permissions from the metadata files, we wouldn't have been able " +
            "to write nor read these documents.", "imported-author", 15);

        assertCollectionSize("The original 'author' collection should not have been affected since we " +
            "specified a different collection for the import.", "author", 15);
    }
}
