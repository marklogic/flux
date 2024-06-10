package com.marklogic.newtool.impl.export;

import com.marklogic.newtool.AbstractTest;
import com.marklogic.newtool.api.SaveMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportParquetFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export-parquet-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".snappy.parquet"));
        assertEquals(1, files.length, "Expecting 1 file as the default, with Spark defaulting to snappy for compression.");

        // Read the files back in to ensure we get 15 rows
        run(
            "import-parquet-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test"
        );

        assertCollectionSize("parquet-test", 15);
    }

    @Test
    void saveMode(@TempDir Path tempDir) {
        String stderr = runAndReturnStderr(() -> run(
            "export-parquet-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--path", tempDir.toFile().getAbsolutePath(),
            "--mode", SaveMode.ERRORIFEXISTS.name()
        ));

        assertTrue(stderr.contains("already exists"), "This test is just verifying that --mode is interpreted " +
            "correctly; unexpected stderr: " + stderr);
    }

    @Test
    void dynamicParameter(@TempDir Path tempDir) {
        run(
            "export-parquet-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "2",
            "--file-count", "2",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-Pcompression=gzip"
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".gz.parquet"));
        assertEquals(2, files.length, "Expecting 2 gzipped Parquet files since --file-count is 2, and the " +
            "-Pcompression option should tell Spark Parquet to use gzip instead of snappy.");
    }
}
