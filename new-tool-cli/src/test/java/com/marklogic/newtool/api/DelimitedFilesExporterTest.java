package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelimitedFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) throws IOException {
        NT.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .readRows(options -> options
                .opticQuery("op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))")
                .partitions(1))
            .writeFiles(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .additionalOptions(Map.of("header", "false")))
            .execute();

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);

        String content = FileCopyUtils.copyToString(new FileReader(files[0]));
        String[] lines = content.split("\n");
        assertEquals(15, lines.length);
        assertEquals("1,Awton,Finlay,2022-07-13,2022-07-13T09:00:00.000Z,4,,,", lines[0]);
    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        NT.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .readRows("op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))")
            .limit(1)
            .writeFiles(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);
    }

    @Test
    void missingPath() {
        DelimitedFilesExporter exporter = NT.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .readRows(READ_AUTHORS_OPTIC_QUERY);

        NtException ex = assertThrowsNtException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
