/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void encoding() {
        ExportArchiveFilesCommand command = (ExportArchiveFilesCommand) getCommand(
            "export-archive-files",
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
        ExportArchiveFilesCommand command = (ExportArchiveFilesCommand) getCommand(
            "export-archive-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--streaming",
            "--categories", "collections,permissions",
            "--no-snapshot"
        );

        assertOptions(command.makeReadOptions(),
            Options.STREAM_FILES, "true",
            Options.READ_DOCUMENTS_COLLECTIONS, "anything",
            Options.READ_SNAPSHOT, "false"
        );

        assertOptions(command.makeWriteOptions(),
            Options.STREAM_FILES, "true",

            // Needed for reading documents and metadata.
            Options.CLIENT_URI, "test:test@host:8000",

            // Needed so that the connector knows what metadata to retrieve.
            Options.READ_DOCUMENTS_CATEGORIES, "content,collections,permissions"
        );
    }
}
