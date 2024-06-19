/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Note that some tests will print a "An illegal reflective access operation has occurred" warning that occurs
 * when running on Spark on Java 9 or higher. Our ETL tool prevents this, so you can ignore it when analyzing the
 * logging in these tests.
 */
class HandleErrorTest extends AbstractTest {

    @Test
    void invalidCommand() {
        assertStderrContains(
            () -> run("not_a_real_command", "--connection-string", makeConnectionString()),
            "Unmatched arguments from index 0: 'not_a_real_command'"
        );
    }

    @Test
    void invalidParam() {
        assertStderrContains(
            () -> run("import-files", "--not-a-real-param", "--path", "anywhere"),
            "Unknown option: '--not-a-real-param'"
        );
    }

    @Test
    void invalidParamWithSingleQuotesInIt() {
        assertStderrContains(
            () -> run("import-files", "-not-a-'real'-param", "--path", "anywhere"),
            "Unknown option: '-not-a-'real'-param'"
        );
    }

    @Test
    void badDynamicOption() {
        assertStderrContains(
            () -> run("import-files", "-CnoValue"),
            "Value for option option '-C' (<String=String>) should be in KEY=VALUE format but was noValue"
        );
    }

    @Test
    void missingRequiredParam() {
        assertStderrContains(
            () -> run("import-files", "--connection-string", makeConnectionString()),
            "Missing required option: '--path <path>'"
        );
    }

    /**
     * In this scenario, Spark throws an error before our connector really does anything.
     */
    @Test
    void sparkFailure() {
        assertStderrContains(
            () -> run(
                "import-files",
                "--path", "/not/valid",
                "--connection-string", makeConnectionString()
            ),
            "Command failed, cause: AnalysisException: [PATH_NOT_FOUND] Path does not exist: file:/not/valid."
        );
    }

    @Test
    void abortOnWriteFailure() {
        assertStderrContains(
            () -> run(
                "import-files",
                "--path", "src/test/resources/mixed-files/hello*",
                "--repartition", "2",
                "--connection-string", makeConnectionString(),
                "--permissions", "invalid-role,read,rest-writer,update",
                "--abort-on-write-failure"
            ),
            "Command failed, cause: Local message: failed to apply resource at documents"
        );
    }

    @Test
    void abortOnWriteFailureAndShowStacktrace() {
        assertStderrContains(
            () -> run(
                "import-files",
                "--path", "src/test/resources/mixed-files/hello*",
                "--repartition", "2",
                "--connection-string", makeConnectionString(),
                "--permissions", "invalid-role,read,rest-writer,update",
                "--stacktrace",
                "--abort-on-write-failure"
            ),
            "com.marklogic.spark.ConnectorException: Local message: failed to apply resource at documents"
        );
    }

    @Test
    void dontAbortOnWriteFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            // Using two partitions to verify that both partition writers log an error.
            "--repartition", "2",
            "--connection-string", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update"
        ));

        assertFalse(stderr.contains("Command failed"), "The command should not have failed since it defaults to not " +
            "aborting on a write failure.");
    }
}
