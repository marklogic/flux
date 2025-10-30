/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.junit5.TestDataReloader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestDataReloader.class)
class ExportOrcFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export-orc-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "4",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".snappy.orc"));
        assertTrue(files.length <= 4, "Expecting at most 1 file per partition. We may get less than 4 if the Optic " +
            "rows are randomly assigned to only 3 partitions instead of 4. Actual count: " + files.length);

        // Read the files back in to ensure we get 15 rows
        run(
            "import-orc-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-test"
        );

        assertCollectionSize("orc-test", 15);
    }

    @Test
    void dynamicParameter(@TempDir Path tempDir) {
        run(
            "export-orc-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "1",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--spark-prop", "compression=lz4"
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".lz4.orc"));
        assertEquals(1, files.length, "Expecting 1 gzipped ORC file, as there is a single partition, and the " +
            "compression option should tell Spark ORC to use lz4 instead of snappy.");
    }
}
