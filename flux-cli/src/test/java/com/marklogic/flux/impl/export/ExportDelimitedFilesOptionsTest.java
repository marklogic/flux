/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportDelimitedFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ExportDelimitedFilesCommand command = (ExportDelimitedFilesCommand) getCommand(
            "export-delimited-files",
            "--connection-string", "test:test@host:8000",
            "--query", "anything",
            "--path", "anywhere",
            "--encoding", "UTF-16",
            "-Pkey=value"
        );

        Map<String, String> options = command.getWriteFilesParams().get();
        assertEquals("true", options.get("header"), "The command should default to including the header, both for " +
            "consistency with MLCP and because it's very common to want a header in a delimited file.");
        assertEquals("value", options.get("key"));
        assertEquals("UTF-16", options.get("encoding"), "--encoding is included for consistency with other commands " +
            "that support a custom encoding, even though a user can also specify it via -Pencoding=");
    }
}
