/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Needs to be updated for picocli")
class HelpTest extends AbstractTest {

    @Test
    void summaryUsage() {
        String stdout = runAndReturnStdout(() -> run());

        assertTrue(stdout.contains("View details for the named command."),
            "Summary usage is expected to show each command its description, but no parameters; stdout: " + stdout);

        assertFalse(stdout.contains("-host"), "No parameters should be shown with summary usage; stdout: " + stdout);
    }

    @Test
    void helpForSingleCommand() {
        String stdout = runAndReturnStdout(() -> run("help", "import-files"));
        assertTrue(stdout.contains("Usage: import-files [options]"));
        assertFalse(stdout.contains("import-jdbc"), "Only the given command should be shown.");
        assertTrue(stdout.contains("-host"));
        assertTrue(stdout.contains("--path"));
    }

    @Test
    void helpForInvalidCommand() {
        String stdout = runAndReturnStdout(() -> run("help", "not_a_real_command"));
        assertTrue(stdout.contains("Unrecognized command name: not_a_real_command"),
            "Unexpected stdout: " + stdout);
    }

    @Test
    void noCommand() {
        String stdout = runAndReturnStdout(() -> run("help"));
        assertTrue(stdout.contains("Usage: help"), "If 'help' is run with no command, then the usage for the " +
            "'help' command should be shown.");
    }
}
