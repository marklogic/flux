package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrcFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.exportOrcFiles()
            .connectionString(makeConnectionString())
            .readRows(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .additionalOptions(Map.of("compression", "lz4"))
            )
            .execute();

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".lz4.orc"));
        assertEquals(1, files.length, "Expecting 1 gzipped ORC file, and the " +
            "-Pcompression option should tell Spark ORC to use lz4 instead of snappy.");
    }
}
