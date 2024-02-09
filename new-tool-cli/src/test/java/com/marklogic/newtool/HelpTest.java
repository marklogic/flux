package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpTest extends AbstractTest {

    @Test
    void summaryUsage() {
        String stdout = runAndReturnStdout(() -> run());

        assertTrue(stdout.contains("View details for the named command."),
            "Summary usage is expected to show each command its description, but no parameters; stdout: " + stdout);

        assertFalse(stdout.contains("-host"), "No parameters should be shown with summary usage; stdout: " + stdout);

        assertTrue(
            stdout.contains("Read delimited text files from local, HDFS, and S3 locations using Spark's \n"),
            "Each command description is expected to wrap at 120 characters. This test may break when new commands " +
                "are introduced with long names, or if the import_delimited_files description changes. If " +
                "so, just update this assertion to capture the description text that occurs before the first newline " +
                "symbol. stdout: " + stdout
        );
    }

    @Test
    void helpForSingleCommand() {
        String stdout = runAndReturnStdout(() -> run("help", "import_files"));
        assertTrue(stdout.contains("Usage: import_files [options]"));
        assertFalse(stdout.contains("import_jdbc"), "Only the given command should be shown.");
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
