/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the essentials for a MarkLogic connection are available.
 */
class ValidateMarkLogicConnectionTest extends AbstractTest {

    @Test
    void noHost() {
        assertStderrContains(
            () -> run("import-files", "--path", "src/test/resources/mixed-files"),
            "Must specify a MarkLogic host via --host or --connection-string."
        );
    }

    @Test
    void noPort() {
        assertStderrContains(
            () -> run("import-files", "--path", "src/test/resources/mixed-files", "--host", getDatabaseClient().getHost()),
            "Must specify a MarkLogic app server port via --port or --connection-string."
        );
    }

    @Test
    void noUsername() {
        assertStderrContains(() -> run(
                "import-files",
                "--path", "src/test/resources/mixed-files",
                "--host", getDatabaseClient().getHost(),
                "--port", Integer.toString(getDatabaseClient().getPort())
            ),
            "Must specify a MarkLogic user via --username when using 'BASIC' or 'DIGEST' authentication."
        );
    }

    @Test
    void badConnectionString() {
        String output = runAndReturnStderr(() -> run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", "admin-missing-password@localhost:8003"
        ));

        assertTrue(output.contains("Invalid value for connection string; must be username:password@host:port"),
            "Unexpected output: " + output + "; this test also confirms that the ETL tool is overriding " +
                "error messages from the connector so that CLI option names appear instead of connector " +
                "option names. This is also confirmed by ErrorMessagesTest.");
    }

    @Test
    void connectionStringWithoutUserOrPassword() {
        String output = runAndReturnStderr(() -> run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", "localhost:8003"
        ));

        assertTrue(
            output.contains(
                "Invalid value for option '--connection-string': Invalid value for connection string; must be username:password@host:port/optionalDatabaseName"
            ),
            "Unexpected output: " + output
        );
    }

    @Test
    void cloudAuthDoesntRequirePort() {
        // The expected error message verifies that the user does not need to provide a port when auth-type=cloud.
        // Otherwise, the validation for a missing port would trigger and no connection attempt would be made.
        assertStderrContains(
            () -> run("import-files",
                "--path", "src/test/resources/mixed-files",
                "--host", getDatabaseClient().getHost(),
                "--auth-type", "cloud",
                "--cloud-api-key", "doesnt-matter-for-this-test"
            ),
            "Unable to call token endpoint at https://localhost/token"
        );
    }
}
