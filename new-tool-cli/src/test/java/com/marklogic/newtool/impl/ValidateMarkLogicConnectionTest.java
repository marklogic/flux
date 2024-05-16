package com.marklogic.newtool.impl;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the essentials for a MarkLogic connection are available.
 */
class ValidateMarkLogicConnectionTest extends AbstractTest {

    @Test
    void noHost() {
        assertStderrContains(
            () -> run("import_files", "--path", "src/test/resources/mixed-files"),
            "Must specify a MarkLogic host via --host or --connectionString."
        );
    }

    @Test
    void noPort() {
        assertStderrContains(
            () -> run("import_files", "--path", "src/test/resources/mixed-files", "--host", getDatabaseClient().getHost()),
            "Must specify a MarkLogic app server port via --port or --connectionString."
        );
    }

    @Test
    void noUsername() {
        assertStderrContains(() -> run(
                "import_files",
                "--path", "src/test/resources/mixed-files",
                "--host", getDatabaseClient().getHost(),
                "--port", Integer.toString(getDatabaseClient().getPort())
            ),
            "Must specify a MarkLogic user via --username when using 'BASIC' or 'DIGEST' authentication."
        );
    }

    @Test
    void badClientUri() {
        String output = runAndReturnStderr(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files",
            "--connectionString", "admin-missing-password@localhost:8003"
        ));

        assertTrue(output.contains("Invalid value for --connectionString"),
            "Unexpected output: " + output + "; this test also confirms that the ETL tool is overriding " +
                "error messages from the connector so that CLI option names appear instead of connector " +
                "option names. This is also confirmed by ErrorMessagesTest.");
    }
}
