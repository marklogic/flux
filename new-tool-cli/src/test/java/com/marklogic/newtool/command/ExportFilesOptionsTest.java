package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExportFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void prettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export_files",
            "--clientUri", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--prettyPrint",
            "--compression", "gzip"
        );

        Map<String, String> options = command.makeWriteOptions();
        assertEquals("true", options.get(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("GZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }

    @Test
    void dontPrettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export_files",
            "--clientUri", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--compression", "zip"
        );

        Map<String, String> options = command.makeWriteOptions();
        assertFalse(options.containsKey(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("ZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }
}
