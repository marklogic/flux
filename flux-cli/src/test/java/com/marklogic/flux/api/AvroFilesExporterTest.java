/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.junit5.TestDataReloader;
import com.marklogic.spark.ConnectorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TestDataReloader.class)
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
    void noQuery() {
        AvroFilesExporter exporter = Flux.exportAvroFiles()
            .connectionString(makeConnectionString())
            .to("build/doesnt-matter");

        ConnectorException ex = assertThrows(ConnectorException.class, () -> exporter.execute());
        assertEquals("Must define an Optic query", ex.getMessage(), "Verifying that a friendly error message that " +
            "doesn't have a connector option name in it is shown. This is expected to be shown for any command " +
            "that depends on running an Optic query.");
    }

    @Test
    void missingPath() {
        AvroFilesExporter exporter = Flux.exportAvroFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY);

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
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
