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
            .readRows(READ_AUTHORS_OPTIC_QUERY)
            .connectionString(makeConnectionString())
            .writeFiles(tempDir.toFile().getAbsolutePath())
            .execute();

        Flux.importJsonFiles()
            .readFiles(options -> options
                .paths(tempDir.toFile().getAbsolutePath())
                .jsonLines(true))
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options
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
            .readRows(READ_AUTHORS_OPTIC_QUERY);

        FluxException ex = assertThrowsNtException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
