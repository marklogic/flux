package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.apache.spark.sql.SaveMode;
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
            "export_parquet_files",
            "--clientUri", makeClientUri(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "3",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".snappy.parquet"));
        assertEquals(3, files.length, "Expecting 1 file per partition, with Spark defaulting to snappy for compression.");

        // Read the files back in to ensure we get 15 rows
        run(
            "import_parquet_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test"
        );

        assertCollectionSize("parquet-test", 15);
    }

    @Test
    void saveMode(@TempDir Path tempDir) {
        String stderr = runAndReturnStderr(() -> run(
            "export_parquet_files",
            "--clientUri", makeClientUri(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--path", tempDir.toFile().getAbsolutePath(),
            "--mode", SaveMode.ErrorIfExists.name()
        ));

        assertTrue(stderr.contains("already exists"), "This test is just verifying that --mode is interpreted " +
            "correctly; unexpected stderr: " + stderr);
    }

    @Test
    void dynamicParameter(@TempDir Path tempDir) {
        run(
            "export_parquet_files",
            "--clientUri", makeClientUri(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "2",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-Pcompression=gzip"
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".gz.parquet"));
        assertEquals(2, files.length, "Expecting 2 gzipped Parquet files, as there are 2 partitions, and the " +
            "-Pcompression option should tell Spark Parquet to use gzip instead of snappy.");
    }
}