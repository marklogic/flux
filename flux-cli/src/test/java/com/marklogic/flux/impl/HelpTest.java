/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

class HelpTest extends AbstractTest {

    @Test
    void returnCode() {
        assertEquals(CommandLine.ExitCode.USAGE, run());
        assertEquals(CommandLine.ExitCode.USAGE, run("help", "import-files"));
    }

    @Test
    void summaryUsage() {
        String stdout = runAndReturnStdout(() -> run());
        assertTrue(stdout.contains("Usage: ./bin/flux [COMMAND]"));
        assertFalse(stdout.contains("-host"), "No parameters should be shown with summary usage; stdout: " + stdout);
    }

    @Test
    void helpForSingleCommand() {
        String stdout = runAndReturnStdout(() -> run("help", "import-files"));
        assertTrue(stdout.contains("Usage: ./bin/flux import-files [OPTIONS]"));
        assertFalse(stdout.contains("import-jdbc"), "Only the given command should be shown.");
        assertTrue(stdout.contains("--host"));
        assertTrue(stdout.contains("--path"));
    }

    @Test
    void helpForInvalidCommand() {
        assertStderrContains(() -> run("help", "not_a_real_command"),
            "Unknown subcommand 'not_a_real_command'.");
    }

    @Test
    void emptyStringCommand() {
        String stdout = runAndReturnStdout(() -> run(""));
        assertTrue(stdout.contains("Usage: ./bin/flux [COMMAND]"));
    }

    @Test
    void noCommandAfterHelp() {
        String stdout = runAndReturnStdout(() -> run("help"));
        System.out.println(stdout);
        assertTrue(stdout.contains("Usage: ./bin/flux help [OPTIONS] [COMMAND]"),
            "If 'help' is run with no command, then the help for 'help' should be shown.");
    }
}
