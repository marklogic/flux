/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        Flux.exportGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author"))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .compressionType(CompressionType.ZIP)
                .zipFileCount(1))
            .execute();

        File dir = tempDir.toFile();
        assertEquals(1, dir.listFiles().length, "Expecting 1 zip file due to the use of zipFileCount(1).");
    }

    @Test
    void pathOnly(@TempDir Path tempDir) {
        Flux.exportGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author"))
            .to(tempDir.toFile().getAbsolutePath())
            .execute();

        File dir = tempDir.toFile();
        File authorsDir = new File(dir, "author");
        assertEquals(15, authorsDir.listFiles().length);
    }

    @Test
    void prettyPrint(@TempDir Path tempDir) throws IOException {
        Flux.exportGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.uris("/author/author1.json"))
            .to(options -> options
                .path(tempDir.toFile().getAbsolutePath())
                .prettyPrint(true))
            .execute();

        File dir = tempDir.toFile();
        File authorFile = new File(new File(dir, "author"), "author1.json");
        assertTrue(authorFile.exists());

        String content = new String(FileCopyUtils.copyToByteArray(authorFile));
        assertTrue(content.startsWith("{\n"), "The content should be pretty-printed and thus have a newline " +
            "after the first opening brace; actual content: " + content);
    }

    @Test
    void missingPath() {
        GenericFilesExporter exporter = Flux.exportGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author"));

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify a file path", ex.getMessage());
    }

    @Test
    void noQuerySpecified() {
        GenericFilesExporter exporter = Flux.exportGenericFiles()
            .connectionString(makeConnectionString())
            .to(options -> options.path("build/doesnt-matter"));

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify at least one of the following for the documents to export: " +
                "collections; a directory; a string query; a structured, serialized, or combined query; or URIs.",
            ex.getMessage());
    }
}
