/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonLinesFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportJsonLinesFiles()
            .from(READ_AUTHORS_OPTIC_QUERY)
            .connectionString(makeConnectionString())
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        Flux.importJsonFiles()
            .from(options -> options
                .paths(tempDir.toFile().getAbsolutePath())
                .jsonLines(true))
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/imported/{LastName}.json")
                .collections("imported-json-lines"))
            .execute();

        assertCollectionSize("imported-json-lines", 15);
    }

    @Test
    void missingPath() {
        JsonLinesFilesExporter exporter = Flux.exportJsonLinesFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY);

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
