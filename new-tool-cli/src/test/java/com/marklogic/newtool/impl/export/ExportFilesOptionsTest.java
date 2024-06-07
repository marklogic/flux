package com.marklogic.newtool.impl.export;

import com.marklogic.newtool.impl.AbstractOptionsTest;
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
            "--connectionString", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--prettyPrint",
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
            "--connectionString", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--compression", "zip"
        );

        Map<String, String> options = command.writeParams.get();
        assertFalse(options.containsKey(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("ZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }
}
