/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
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
            "export-json-lines-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--file-count", "2",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] jsonFiles = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertEquals(2, jsonFiles.length);

        // Import in to verify.
        run(
            "import-aggregate-json-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--json-lines",
            "--connection-string", makeConnectionString(),
            "--collections", "imported-json",
            "--uri-template", "/imported/{LastName}.json",
            "--permissions", DEFAULT_PERMISSIONS
        );

        assertCollectionSize("imported-json", 15);
    }
}
