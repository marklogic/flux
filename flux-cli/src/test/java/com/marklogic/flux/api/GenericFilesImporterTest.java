/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GenericFilesImporterTest extends AbstractTest {

    private static final String PATH = "src/test/resources/mixed-files/hello*";

    @Test
    void test() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(PATH)
            // Including streaming just for smoke testing and manual inspection of log messages.
            .streaming()
            .to(options -> options
                .collectionsString("api-files,second-collection")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("api-files", 4);
        assertCollectionSize("second-collection", 4);
    }

    @Test
    void zipFile() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/mixed-files/goodbye.zip")
                .compressionType(CompressionType.ZIP))
            .to(options -> options
                .collections("files")
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriReplace(".*/mixed-files,''"))
            .execute();

        assertCollectionSize("files", 3);
        Stream.of("/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml").forEach(uri ->
            assertInCollections(uri, "files"));
    }

    @Test
    void documentType() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/mixed-files/hello.json")
            .to(options -> options
                .collectionsString("api-files,second-collection")
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriReplace(".*/mixed-files,''")
                .uriSuffix(".unknown")
                .documentType(GenericFilesImporter.DocumentType.JSON))
            .execute();

        String kind = getDatabaseClient().newServerEval()
            .xquery("xdmp:node-kind(doc('/hello.json.unknown')/node())")
            .evalAs(String.class);
        assertEquals("object", kind, "Forcing the document type to JSON should result in a document with " +
            "an unknown extension - in this case, 'unknown' - to be treated as JSON.");
    }

    @Test
    void badPath() {
        GenericFilesImporter command = Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("path/doesnt/exist");

        FluxException ex = assertThrowsFluxException(command::execute);
        assertTrue(ex.getMessage().contains("Path does not exist"),
            "Unexpected message: " + ex.getMessage() + ". And I'm not sure we want this Spark-specific " +
                "exception to escape. Think we need for AbstractCommand to catch Throwable and look for a " +
                "Spark-specific exception. If one is found, we need a new generic 'Flux' runtime exception to throw " +
                "with the Spark-specific exception message.");
    }

    @Test
    void abortOnWriteFailure() {
        GenericFilesImporter command = Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from(options -> options.paths(PATH))
            .to(options -> options
                .abortOnWriteFailure(true)
                .permissionsString("not-a-real-role,update"));

        FluxException ex = assertThrows(FluxException.class, command::execute);
        assertTrue(ex.getMessage().contains("Role does not exist"), "Unexpected error: " + ex.getMessage());
    }

    @Test
    void missingPath() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void extractText() {
        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/extraction-files/hello-world.docx")
            .to(options -> options
                .collections("binary")
                .permissionsString(DEFAULT_PERMISSIONS)
                .extractText()
                .extractedTextPermissionsString(DEFAULT_PERMISSIONS)
                .extractedTextCollections("extracted-text")
                .extractedTextDocumentType(GenericFilesImporter.DocumentType.XML)
                .extractedTextDropSource()
            )
            .execute();

        assertCollectionSize("The original docx file should have been dropped", "binary", 0);
        assertCollectionSize("extracted-text", 1);
    }
}
