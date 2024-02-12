package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the essentials for a MarkLogic connection are available.
 */
class ValidateMarkLogicConnectionTest extends AbstractTest {

    @Test
    void noHost() {
        String output = runAndReturnStderr(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files"
        ));

        assertTrue(output.contains("Must specify a MarkLogic host via --host or --clientUri."),
            "Unexpected stderr: " + output);
    }

    @Test
    void noPort() {
        String output = runAndReturnStderr(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files",
            "--host", getDatabaseClient().getHost()
        ));

        assertTrue(output.contains("Must specify a MarkLogic app server port via --port or --clientUri."),
            "Unexpected stderr: " + output);
    }

    @Test
    void noUsername() {
        String output = runAndReturnStderr(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files",
            "--host", getDatabaseClient().getHost(),
            "--port", Integer.toString(getDatabaseClient().getPort())
        ));

        assertTrue(output.contains("Must specify a MarkLogic user via --username when using 'BASIC' or 'DIGEST' authentication."),
            "Unexpected stderr: " + output);
    }

    /**
     * This shows a gap where errors from the Spark connector can reference Spark option names that don't make any sense
     * to an ETL user. MLE-12333 will improve this.
     */
    @Test
    void badClientUri() {
        String output = runAndReturnStderr(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files",
            "--clientUri", "admin-missing-password@localhost:8003"
        ));

        assertTrue(output.contains("Invalid value for spark.marklogic.client.uri"), "Unexpected output: " + output);
    }
}
