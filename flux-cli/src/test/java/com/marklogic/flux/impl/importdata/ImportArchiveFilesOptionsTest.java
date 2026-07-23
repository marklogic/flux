/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImportArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--preview", "10",
            "--partitions", "18",
            "--encoding", "UTF-16"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "18",
            Options.READ_FILES_ENCODING, "UTF-16"
        );
    }

    @Test
    void streaming() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--streaming",
            "--streaming-transform-binary-with-extension", "json,xml",
            "--document-type", "xml"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.STREAM_FILES, "true",
            Options.READ_FILES_TYPE, "archive"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.STREAM_FILES, "true",
            Options.STREAM_TRANSFORM_BINARY_EXTENSIONS, "json,xml",
            Options.WRITE_DOCUMENT_TYPE, "XML"
        );
    }

    @Test
    void zipProtectionOptions() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--zip-max-entry-bytes", "268435456",
            "--zip-max-entry-count", "100000"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES, "268435456",
            Options.READ_ZIP_MAX_ENTRY_COUNT, "100000"
        );
    }

    @Test
    void zipProtectionOptionsNotSetByDefault() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES),
            "Zip byte limit should not be set when the flag is omitted.");
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_ENTRY_COUNT),
            "Zip entry count limit should not be set when the flag is omitted.");
    }

    @Test
    void zipMaxEntryBytesZeroThrowsFluxException() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--zip-max-entry-bytes", "0"
        );
        FluxException ex = assertThrows(FluxException.class, () -> command.getReadParams().makeOptions());
        assertTrue(ex.getMessage().contains("--zip-max-entry-bytes"));
    }

    @Test
    void zipMaxEntryBytesNegativeThrowsFluxException() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--zip-max-entry-bytes", "-5"
        );
        FluxException ex = assertThrows(FluxException.class, () -> command.getReadParams().makeOptions());
        assertTrue(ex.getMessage().contains("--zip-max-entry-bytes"));
    }

    @Test
    void zipMaxEntryCountZeroThrowsFluxException() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--zip-max-entry-count", "0"
        );
        FluxException ex = assertThrows(FluxException.class, () -> command.getReadParams().makeOptions());
        assertTrue(ex.getMessage().contains("--zip-max-entry-count"));
    }

    @Test
    void zipMaxEntryCountNegativeThrowsFluxException() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/archive-files",
            "--zip-max-entry-count", "-5"
        );
        FluxException ex = assertThrows(FluxException.class, () -> command.getReadParams().makeOptions());
        assertTrue(ex.getMessage().contains("--zip-max-entry-count"));
    }
}
