package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportAvroFilesTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        run(
            "export_avro_files",
            "--clientUri", makeClientUri(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "4",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".avro"));
        assertEquals(4, files.length, "Expecting 1 file per partition.");

        // Read the files back in to ensure we get 15 rows
        run(
            "import_avro_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--clientUri", makeClientUri(),
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
            "export_avro_files",
            "--clientUri", makeClientUri(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--partitions", "2",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-PavroSchema=intentionally-invalid"
        ));

        assertTrue(stderr.contains("Command failed, cause: com.fasterxml.jackson.core.JsonParseException"),
            "This test is verifying that -P params are passed to the Avro data source. Since an invalid " +
                "Avro schema is being set, this test expects an error. Unexpected stderr: " + stderr);
    }
}
