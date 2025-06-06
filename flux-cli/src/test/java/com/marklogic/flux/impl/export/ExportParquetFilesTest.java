/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.junit5.TestDataReloader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestDataReloader.class)
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

        assertEquals(1, tempDir.toFile().listFiles().length, "Flux should not include Spark SUCCESS " +
            "files, nor checksum (.crc) files. Should just have the one .snappy.parquet file. We may later expose " +
            "configuration options for allowing these files to be created, if we get feedback asking for that.");

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
        String stderr = runAndReturnStderr(
            "export-parquet-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--path", tempDir.toFile().getAbsolutePath(),
            "--mode", SaveMode.ERRORIFEXISTS.name()
        );

        assertTrue(stderr.contains("already exists"), "This test is just verifying that --mode is interpreted " +
            "correctly; unexpected stderr: " + stderr);
        assertTrue(tempDir.toFile().exists(), "Verifying the temp dir exists, which also prevents Sonar from " +
            "complaining that the tempDir isn't used (even though it is above).");
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
