package com.marklogic.newtool.impl;

import com.marklogic.newtool.AbstractTest;
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
            () -> run("not_a_real_command", "--connectionString", makeConnectionString()),
            "Invalid command name: not_a_real_command"
        );
    }

    @Test
    void invalidParam() {
        assertStderrContains(
            () -> run("import-files", "--not-a-real-param"),
            "Invalid option: '--not-a-real-param'"
        );
    }

    @Test
    void invalidParamWithSingleQuotesInIt() {
        assertStderrContains(
            () -> run("import-files", "-not-a-'real'-param"),
            "Invalid option: '-not-a-'real'-param'"
        );
    }

    @Test
    void badDynamicOption() {
        assertStderrContains(
            () -> run("import-files", "-CnoValue"),
            "Options specified via '-C' or '-P' must have a form of -Ckey=value or -Pkey=value."
        );
    }

    @Test
    void missingRequiredParam() {
        assertStderrContains(
            () -> run("import-files", "--connectionString", makeConnectionString()),
            "The following option is required: [--path]"
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
                "--connectionString", makeConnectionString()
            ),
            "Command failed, cause: [PATH_NOT_FOUND] Path does not exist: file:/not/valid."
        );
    }

    @Test
    void abortOnWriteFailure() {
        assertStderrContains(
            () -> run(
                "import-files",
                "--path", "src/test/resources/mixed-files/hello*",
                "--repartition", "2",
                "--connectionString", makeConnectionString(),
                "--permissions", "invalid-role,read,rest-writer,update",
                "--abortOnWriteFailure"
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
                "--connectionString", makeConnectionString(),
                "--permissions", "invalid-role,read,rest-writer,update",
                "--stacktrace",
                "--abortOnWriteFailure"
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
            "--connectionString", makeConnectionString(),
            "--permissions", "invalid-role,read,rest-writer,update"
        ));

        assertFalse(stderr.contains("Command failed"), "The command should not have failed since it defaults to not " +
            "aborting on a write failure.");
    }
}
