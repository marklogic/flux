/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note that some tests will print a "An illegal reflective access operation has occurred" warning that occurs
 * when running on Spark on Java 9 or higher. The Flux CLI prevents this, so you can ignore it when analyzing the
 * logging in these tests.
 */
class HandleErrorTest extends AbstractTest {

    @Test
    void invalidCommand() {
        assertStderrContains(
            "Unmatched arguments from index 0: 'not_a_real_command'",
            CommandLine.ExitCode.USAGE,
            "not_a_real_command", "--connection-string", makeConnectionString()
        );
    }

    @Test
    void missingOptionsFile() {
        assertStderrContains(
            "Please ensure the options file you are referencing exists and is readable.",

            CommandLine.ExitCode.USAGE,
            "import-files",
            "--connection-string", makeConnectionString(),
            "@no-such-options.txt",
            "--path", "somewhere"
        );
    }

    @Test
    void invalidParam() {
        assertStderrContains(
            "Unknown option: '--not-a-real-param'",
            CommandLine.ExitCode.USAGE,
            "import-files", "--not-a-real-param", "--path", "anywhere"
        );
    }

    @Test
    void invalidParamWithSingleQuotesInIt() {
        assertStderrContains(
            "Unknown option: '-not-a-'real'-param'",
            CommandLine.ExitCode.USAGE,
            "import-files", "-not-a-'real'-param", "--path", "anywhere"
        );
    }

    @Test
    void badDynamicOption() {
        assertStderrContains(
            "Value for option '--spark-conf' (<String=String>) should be in KEY=VALUE format but was noValue",
            CommandLine.ExitCode.USAGE, "import-files", "--spark-conf", "noValue"
        );
    }

    @Test
    void missingRequiredParam() {
        assertStderrContains(
            "Missing required option: '--path <path>'",
            CommandLine.ExitCode.USAGE,
            "import-files", "--connection-string", makeConnectionString()
        );

        assertStderrContains(
            "Run './bin/flux help import-files' for more information.",
            "import-files", "--connection-string", makeConnectionString()
        );
    }

    @Test
    void buildCommandContextWithMissingRequiredOption() {
        String[] args = {"import-files", "--connection-string", makeConnectionString()};
        Main main = new Main();
        CommandLine.ParameterException ex = assertThrows(CommandLine.ParameterException.class, () -> main.buildCommandContext(args));
        assertEquals("Missing required option: '--path <path>'", ex.getMessage(),
            "Verifies that for standard parameter exceptions thrown by picocli will be propagated instead of being " +
                "written to stderr.");
    }

    @Test
    void buildCommandContextWithInvalidConnectionOptions() {
        String[] args = {"import-files", "--path", "/tmp", "--port", "8000"};
        Main main = new Main();
        FluxException ex = assertThrowsFluxException(() -> main.buildCommandContext(args));
        assertEquals("Must specify a MarkLogic host via --host or --connection-string.", ex.getMessage(),
            "Verifies that if there's any issue with the args, an exception is thrown instead of being written " +
                "to stderr. This should be more useful in a context where a command is being run programmatically " +
                "via command line options.");
    }

    /**
     * In this scenario, Spark throws an error before our connector really does anything.
     */
    @Test
    void sparkFailure() {
        assertStderrContains(
            "Error: AnalysisException: [PATH_NOT_FOUND] Path does not exist: file:/not/valid.",
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "--path", "/not/valid",
            "--connection-string", makeConnectionString()
        );
    }

    /**
     * Verifies that if an IAE is thrown, its class name is not logged because the error message is expected to be
     * helpful enough without exposing the exception class name.
     */
    @Test
    void illegalArgumentException() {
        assertStderrContains(
            "Error: Either access key or SAS token must be provided for Azure Blob Storage.",
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--azure-storage-account", "something",
            "--azure-container-name", "something"
        );
    }

    @Test
    void abortOnWriteFailure() {
        assertStderrContains(
            "Role does not exist: sec:role-name = invalid-role",
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--repartition", "2",
            "--connection-string", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update",
            "--abort-on-write-failure",
            // Including this to ensure it doesn't cause any problems. Not yet able to capture its output as it's
            // via a logger.
            "--stacktrace"
        );
    }

    @Test
    void abortOnWriteFailureAndShowStacktrace() {
        assertStderrContains(
            "Role does not exist: sec:role-name = invalid-role",
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--repartition", "2",
            "--connection-string", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update",
            "--stacktrace",
            "--abort-on-write-failure"
        );
    }

    @Test
    void dontAbortOnWriteFailure() {
        String stderr = runAndReturnStderr(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            // Using two partitions to verify that both partition writers log an error.
            "--repartition", "2",
            "--connection-string", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update"
        );

        assertFalse(stderr.contains("Command failed"), "The command should not have failed since it defaults to not " +
            "aborting on a write failure. Actual stderr: " + stderr);
    }

    @ParameterizedTest
    @CsvSource({
        "-P,--spark-prop",
        "-E,--embedder-prop",
        "-L,--classifier-prop",
        "-S,--splitter-prop",
        "-R,--doc-prop",
        "-M,--doc-metadata",
        "-X,--xpath-namespace",
    })
    void niceErrorMessageForShortOptionsRemovedInTwoDotZeroRelease(String shortOption, String longOption) {
        // Verifying we get a helpful error message for each short option removed in the 2.0 release. The command here
        // is not important.
        String stderr = runAndReturnStderr(
            CommandLine.ExitCode.USAGE,
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "%stheKey=theValue".formatted(shortOption)
        );

        assertTrue(stderr.contains("Did you mean to use %s instead of %s, as %s was replaced in the 2.0 release with %s?"
            .formatted(longOption, shortOption, shortOption, longOption)), "Unexpected stderr: " + stderr);

        assertTrue(stderr.contains("If so, use %s theKey=theValue instead.".formatted(longOption)),
            "Unexpected stderr: " + stderr);
    }

    /**
     * The ConnectionParams ArgGroup is very handy because it allows for ordering and grouping options when a user
     * views the "help" for a command. However, picocli has a quirk where if an option in an ArgGroup is specified
     * multiple times, the error message is not very helpful. This test verifies that our custom error handling
     * converts the picocli message into something more user friendly.
     */
    @Test
    void duplicateOptionFromArgGroup() {
        assertStderrContains(
            "option '--port' should be specified only once",
            CommandLine.ExitCode.USAGE,
            "import-files",
            "--path", "build",
            "--host", "localhost",
            "--port", "8000",
            "--port", "8000"
        );

        assertStderrContains(
            "option '--connection-string' should be specified only once",
            CommandLine.ExitCode.USAGE,
            "import-files",
            "--path", "build",
            "--connection-string", makeConnectionString(),
            "--connection-string", makeConnectionString()
        );
    }
}
