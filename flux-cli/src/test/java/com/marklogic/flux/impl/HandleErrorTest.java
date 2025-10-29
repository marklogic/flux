/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
            "Unmatched arguments from index 0: 'not_a_real_command'",
            CommandLine.ExitCode.USAGE,
            "not_a_real_command", "--connection-string", makeConnectionString()
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
}
