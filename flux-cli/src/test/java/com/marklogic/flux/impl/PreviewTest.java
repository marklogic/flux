/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreviewTest extends AbstractTest {

    @Test
    void limitAndPreview() {
        run(
            "import-delimited-files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--connection-string", makeConnectionString(),
            "--limit", "1",
            "--preview", "3",
            "--preview-list",
            "--collections", "sample"
        );

        // We have not had reliable success with capturing stdout and making assertions on it, so this just verifies
        // that nothing is written.
        assertCollectionSize("Verifying that nothing is written to the collection", "sample", 0);
    }

    @Test
    void preview() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--preview", "2",
            "--preview-drop", "content", "modificationTime",
            "--preview-list",
            "--collections", "sample"
        );

        assertCollectionSize("Verifying that nothing is written to the collection", "sample", 0);
    }

    @Test
    void previewSchema(@TempDir Path tempDir) {
        run(
            "export-parquet",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--query", "op.fromView('Medical', 'Authors')",
            "--preview-schema"
        );

        assertEquals(0, tempDir.toFile().listFiles().length,
            "Verifying that nothing was exported since preview-schema was used, which should result in the Spark " +
                "dataset schema being logged at the INFO level (which we're not yet able to assert on).");
    }
}
