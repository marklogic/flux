/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
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
        Flux.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .opticQuery("op.fromView('Medical', 'Authors', '')" +
                    ".orderBy(op.asc(op.col('LastName')))" +
                    ".select(['CitationID', 'LastName', 'ForeName'])")
                .partitions(1))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .fileCount(1)
                .additionalOptions(Map.of("header", "false")))
            .execute();

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);

        String content = FileCopyUtils.copyToString(new FileReader(files[0]));
        String[] lines = content.split("\n");
        assertEquals(15, lines.length);
        assertEquals("1,Awton,Finlay", lines[0]);
    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        Flux.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .from("op.fromView('Medical', 'Authors', '')" +
                ".orderBy(op.asc(op.col('LastName')))" +
                ".select(['LastName'])")
            .limit(1)
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);
    }

    @Test
    void missingPath() {
        DelimitedFilesExporter exporter = Flux.exportDelimitedFiles()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY);

        FluxException ex = assertThrowsFluxException(exporter::execute);
        assertEquals("Must specify a file path", ex.getMessage());
    }
}
