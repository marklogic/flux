/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExportFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void prettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--pretty-print",
            "--compression", "gzip"
        );

        Map<String, String> options = command.writeParams.get();
        assertEquals("true", options.get(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("GZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }

    @Test
    void dontPrettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--compression", "zip"
        );

        Map<String, String> options = command.writeParams.get();
        assertFalse(options.containsKey(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("ZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }

    @Test
    void encoding() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--encoding", "ISO-8859-1"
        );

        Map<String, String> options = command.writeParams.get();
        assertEquals("ISO-8859-1", options.get(Options.WRITE_FILES_ENCODING));
    }

    @Test
    void streaming() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--streaming",
            "--no-snapshot"
        );

        Map<String, String> readOptions = command.buildReadOptions();
        assertEquals("true", readOptions.get(Options.STREAM_FILES));
        assertEquals("false", readOptions.get(Options.READ_SNAPSHOT));
        assertEquals("anything", readOptions.get(Options.READ_DOCUMENTS_COLLECTIONS));

        Map<String, String> writeOptions = command.buildWriteOptions();
        assertEquals("true", writeOptions.get(Options.STREAM_FILES));
        assertEquals("test:test@host:8000", writeOptions.get(Options.CLIENT_URI),
            "The connection options must be present in the write options so that the writer can connect " +
                "to MarkLogic and read documents.");
    }
}
