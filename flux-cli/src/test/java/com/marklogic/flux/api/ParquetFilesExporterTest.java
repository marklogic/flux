/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParquetFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportParquetFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(2)
                .additionalOptions(Map.of("compression", "gzip"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void queryOnly(@TempDir Path tempDir) {
        Flux.exportParquetFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(2)
                .additionalOptions(Map.of("compression", "gzip"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        Flux.exportParquetFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .limit(1)
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".parquet"));
        assertEquals(1, files.length);
    }

    private void verifyFiles(Path tempDir) {
        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".gz.parquet"));
        assertEquals(2, files.length, "Expecting 2 gzipped Parquet files since --file-count is 2, and the " +
            "-Pcompression option should tell Spark Parquet to use gzip instead of snappy.");
    }
}
