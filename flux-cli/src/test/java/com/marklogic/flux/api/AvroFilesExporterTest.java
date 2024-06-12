package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportAvroFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .opticQuery(READ_AUTHORS_OPTIC_QUERY)
                .partitions(2))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1))
            .execute();

        verifyFiles(tempDir);
    }

    @Test
    void queryOnly(@TempDir Path tempDir) {
        Flux.exportAvroFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".avro"));
        assertEquals(1, files.length);
    }

    @Test
    void missingPath() {
        AvroFilesExporter exporter = Flux.exportAvroFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY);

        FluxException ex = assertThrowsNtException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }

    private void verifyFiles(Path tempDir) {
        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".avro"));
        assertEquals(1, files.length);

        // Read the files back in to ensure we get 15 rows
        Flux.importAvroFiles()
            .from(tempDir.toFile().getAbsolutePath())
            .connectionString(makeConnectionString())
            .to(options -> options.permissionsString(DEFAULT_PERMISSIONS).collections("avro-test"))
            .execute();

        assertCollectionSize("avro-test", 15);
    }
}
