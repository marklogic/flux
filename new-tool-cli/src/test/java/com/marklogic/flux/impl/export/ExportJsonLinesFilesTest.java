package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportJsonLinesFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) throws Exception {
        run(
            "export_json_lines_files",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--fileCount", "2",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] jsonFiles = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertEquals(2, jsonFiles.length);

        // Import in to verify.
        run(
            "import_json_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--jsonLines",
            "--connectionString", makeConnectionString(),
            "--collections", "imported-json",
            "--uriTemplate", "/imported/{LastName}.json",
            "--permissions", DEFAULT_PERMISSIONS
        );

        assertCollectionSize("imported-json", 15);
    }
}
