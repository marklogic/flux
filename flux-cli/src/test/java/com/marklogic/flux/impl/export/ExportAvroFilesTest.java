/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportAvroFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export-avro-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "4",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".avro"));
        assertTrue(files.length <= 4, "Expecting at most 1 file per partition. We may get less than 4 if the Optic " +
            "rows are randomly assigned to only 3 partitions instead of 4. Actual count: " + files.length);

        // Read the files back in to ensure we get 15 rows
        run(
            "import-avro-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test"
        );

        assertCollectionSize("avro-test", 15);
    }

    /**
     * Unlike Parquet and ORC files, setting a different compression doesn't result in a different filename that can
     * be asserted on when using Avro. So to verify that dynamic params are recognized, this intentionally causes an
     * error with a dynamic param.
     *
     * @param tempDir
     */
    @Test
    void dynamicParameter(@TempDir Path tempDir) {
        String stderr = runAndReturnStderr(() -> run(
            "export-avro-files",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "2",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-PavroSchema=intentionally-invalid"
        ));

        assertTrue(stderr.contains("Error: SchemaParseException: com.fasterxml.jackson.core.JsonParseException"),
            "This test is verifying that -P params are passed to the Avro data source. Since an invalid " +
                "Avro schema is being set, this test expects an error. Unexpected stderr: " + stderr);
    }
}
