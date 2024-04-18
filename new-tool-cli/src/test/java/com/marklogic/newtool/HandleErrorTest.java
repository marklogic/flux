package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

/**
 * Having issues with capturing logs written by log4j2. So for now, these are just for manual testing - i.e. look at
 * the logs to see how the errors are being displayed.
 * <p>
 * Note that each test will also show the "An illegal reflective access operation has occurred" warning that occurs
 * when running on Spark on Java 9 or higher. Our ETL tool prevents this, so you can ignore it when analyzing the
 * logging in these tests.
 */
// Suppressing Sonar warnings about tests not having assertions as that is not yet possible with this test.
@SuppressWarnings("java:S2699")
class HandleErrorTest extends AbstractTest {

    @Test
    void invalidCommand() {
        run(
            "not_a_real_command",
            "--clientUri", makeClientUri()
        );
    }

    @Test
    void missingRequiredParam() {
        run(
            "import_files",
            "--clientUri", makeClientUri()
        );
    }

    /**
     * In this scenario, Spark throws an error before our connector really does anything.
     */
    @Test
    void sparkFailure() {
        run(
            "import_files",
            "--path", "/not/valid",
            "--clientUri", makeClientUri()
        );
    }

    /**
     * Shows the logging for when the command fails and stops execution.
     */
    @Test
    void abortOnWriteFailure() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--repartition", "2",
            "--clientUri", makeClientUri(),
            "--permissions", "invalid-role,read,rest-writer,update",
            "--abortOnWriteFailure"
        );
    }

    @Test
    void abortOnWriteFailureAndShowStacktrace() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--repartition", "2",
            "--clientUri", makeClientUri(),
            "--permissions", "invalid-role,read,rest-writer,update",
            "--stacktrace",
            "--abortOnWriteFailure"
        );
    }

    @Test
    void dontAbortOnFailure() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            // Using two partitions to verify that both partition writers log an error.
            "--repartition", "2",
            "--clientUri", makeClientUri(),
            "--permissions", "invalid-role,read,rest-writer,update"
        );
    }
}
