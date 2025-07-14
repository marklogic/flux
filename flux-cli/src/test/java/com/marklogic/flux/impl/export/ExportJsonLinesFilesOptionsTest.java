/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportJsonLinesFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ExportJsonLinesFilesCommand command = (ExportJsonLinesFilesCommand) getCommand(
            "export-json-lines-files",
            "--connection-string", "test:test@host:8000",
            "--query", "anything",
            "--path", "anywhere",
            "--encoding", "UTF-16",
            "-Pkey=value",
            "--partitions", "7"
        );

        Map<String, String> options = command.getWriteFilesParams().get();
        assertEquals("value", options.get("key"));
        assertEquals("UTF-16", options.get("encoding"), "--encoding is included for consistency with other commands " +
            "that support a custom encoding, even though a user can also specify it via -Pencoding=");

        assertEquals("local[7]", command.determineSparkMasterUrl());
    }
}
