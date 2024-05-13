package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParquetFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.exportParquetFiles()
            .connectionString(makeConnectionString())
            .readRows(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(2)
                .additionalOptions(Map.of("compression", "gzip"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void queryOnly(@TempDir Path tempDir) {
        NT.exportParquetFiles()
            .connectionString(makeConnectionString())
            .readRows(READ_AUTHORS_OPTIC_QUERY)
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(2)
                .additionalOptions(Map.of("compression", "gzip"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    private void verifyFiles(Path tempDir) {
        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".gz.parquet"));
        assertEquals(2, files.length, "Expecting 2 gzipped Parquet files since --fileCount is 2, and the " +
            "-Pcompression option should tell Spark Parquet to use gzip instead of snappy.");
    }
}
