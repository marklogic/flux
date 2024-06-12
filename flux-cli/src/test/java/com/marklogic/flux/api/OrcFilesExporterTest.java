package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrcFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportOrcFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .additionalOptions(Map.of("compression", "lz4"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void queryOnly(@TempDir Path tempDir) {
        Flux.exportOrcFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .additionalOptions(Map.of("compression", "lz4"))
            )
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        Flux.exportOrcFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .limit(1)
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".orc"));
        assertEquals(1, files.length);
    }

    private void verifyFiles(Path tempDir) {
        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".lz4.orc"));
        assertEquals(1, files.length, "Expecting 1 gzipped ORC file, and the " +
            "-Pcompression option should tell Spark ORC to use lz4 instead of snappy.");
    }
}
