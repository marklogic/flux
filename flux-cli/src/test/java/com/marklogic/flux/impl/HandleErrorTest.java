/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Note that some tests will print a "An illegal reflective access operation has occurred" warning that occurs
 * when running on Spark on Java 9 or higher. The Flux CLI prevents this, so you can ignore it when analyzing the
 * logging in these tests.
 */
class HandleErrorTest extends AbstractTest {

    @Test
    void invalidCommand() {
        assertStderrContains(
            () -> run(CommandLine.ExitCode.USAGE, "not_a_real_command", "--connection-string", makeConnectionString()),
            "Unmatched arguments from index 0: 'not_a_real_command'"
        );
    }

    @Test
    void invalidParam() {
        assertStderrContains(
            () -> run(CommandLine.ExitCode.USAGE, "import-files", "--not-a-real-param", "--path", "anywhere"),
            "Unknown option: '--not-a-real-param'"
        );
    }

    @Test
    void invalidParamWithSingleQuotesInIt() {
        assertStderrContains(
            () -> run(CommandLine.ExitCode.USAGE, "import-files", "-not-a-'real'-param", "--path", "anywhere"),
            "Unknown option: '-not-a-'real'-param'"
        );
    }

    @Test
    void badDynamicOption() {
        assertStderrContains(
            () -> run(CommandLine.ExitCode.USAGE, "import-files", "-CnoValue"),
            "Value for option '-C' (<String=String>) should be in KEY=VALUE format but was noValue"
        );
    }

    @Test
    void missingRequiredParam() {
        assertStderrContains(
            () -> run(CommandLine.ExitCode.USAGE, "import-files", "--connection-string", makeConnectionString()),
            "Missing required option: '--path <path>'"
        );

        assertStderrContains(
            () -> run("import-files", "--connection-string", makeConnectionString()),
            "Run './bin/flux help import-files' for more information."
        );
    }

    /**
     * In this scenario, Spark throws an error before our connector really does anything.
     */
    @Test
    void sparkFailure() {
        assertStderrContains(
            () -> run(
                CommandLine.ExitCode.SOFTWARE,
                "import-files",
                "--path", "/not/valid",
                "--connection-string", makeConnectionString()
            ),
            "Error: AnalysisException: [PATH_NOT_FOUND] Path does not exist: file:/not/valid."
        );
    }

    @Test
    void abortOnWriteFailure() {
        assertStderrContains(
            () -> run(
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
            ),
            "Role does not exist: sec:role-name = invalid-role"
        );
    }

    @Test
    void abortOnWriteFailureAndShowStacktrace() {
        assertStderrContains(
            () -> run(
                CommandLine.ExitCode.SOFTWARE,
                "import-files",
                "--path", "src/test/resources/mixed-files/hello*",
                "--repartition", "2",
                "--connection-string", makeConnectionString(),
                "--permissions", "invalid-role,read,rest-writer,update",
                "--stacktrace",
                "--abort-on-write-failure"
            ),
            "Role does not exist: sec:role-name = invalid-role"
        );
    }

    @Test
    void dontAbortOnWriteFailure() {
        String stderr = runAndReturnStderr(() -> run(
            CommandLine.ExitCode.OK,
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            // Using two partitions to verify that both partition writers log an error.
            "--repartition", "2",
            "--connection-string", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update"
        ));

        assertFalse(stderr.contains("Command failed"), "The command should not have failed since it defaults to not " +
            "aborting on a write failure. Actual stderr: " + stderr);
    }
}
