package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.exportAvroFiles()
            .connectionString(makeConnectionString())
            .readRows(options -> options
                .opticQuery(READ_AUTHORS_OPTIC_QUERY)
                .partitions(4))
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1))
            .execute();

        File[] files = tempDir.toFile().listFiles(file -> file.getName().endsWith(".avro"));
        assertEquals(1, files.length);

        // Read the files back in to ensure we get 15 rows
        NT.importAvroFiles()
            .readFiles(options -> options.paths(tempDir.toFile().getAbsolutePath()))
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options.permissionsString(DEFAULT_PERMISSIONS).collections("avro-test"))
            .execute();

        assertCollectionSize("avro-test", 15);
    }
}
