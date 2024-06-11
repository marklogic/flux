package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportOrcFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export_orc_files",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "4",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".snappy.orc"));
        assertTrue(files.length <= 4, "Expecting at most 1 file per partition. We may get less than 4 if the Optic " +
            "rows are randomly assigned to only 3 partitions instead of 4. Actual count: " + files.length);

        // Read the files back in to ensure we get 15 rows
        run(
            "import_orc_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-test"
        );

        assertCollectionSize("orc-test", 15);
    }

    @Test
    void dynamicParameter(@TempDir Path tempDir) {
        run(
            "export_orc_files",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "1",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-Pcompression=lz4"
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".lz4.orc"));
        assertEquals(1, files.length, "Expecting 1 gzipped ORC file, as there is a single partition, and the " +
            "-Pcompression option should tell Spark ORC to use lz4 instead of snappy.");
    }
}
