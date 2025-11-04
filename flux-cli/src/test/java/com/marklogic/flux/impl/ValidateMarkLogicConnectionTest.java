/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
            "Must specify a MarkLogic host via --host or --connection-string.",
            "import-files", "--path", "src/test/resources/mixed-files"
        );
    }

    @Test
    void noPort() {
        assertStderrContains(
            "Must specify a MarkLogic app server port via --port or --connection-string.",
            "import-files", "--path", "src/test/resources/mixed-files", "--host", getDatabaseClient().getHost()
        );
    }

    @Test
    void noUsername() {
        assertStderrContains(
            "Must specify a MarkLogic user via --username when using 'BASIC' or 'DIGEST' authentication.",
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--host", getDatabaseClient().getHost(),
            "--port", Integer.toString(getDatabaseClient().getPort())
        );
    }

    @Test
    void badConnectionString() {
        String output = runAndReturnStderr(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", "admin-missing-password@localhost:8003"
        );

        assertTrue(output.contains("Invalid value for connection string; must be username:password@host:port"),
            "Unexpected output: " + output + "; this test also confirms that the ETL tool is overriding " +
                "error messages from the connector so that CLI option names appear instead of connector " +
                "option names. This is also confirmed by ErrorMessagesTest.");
    }

    @Test
    void connectionStringWithoutUserOrPassword() {
        String output = runAndReturnStderr(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", "localhost:8003"
        );

        assertTrue(
            output.contains(
                "Invalid value for option '--connection-string': Invalid value for connection string; must be username:password@host:port/optionalDatabaseName"
            ),
            "Unexpected output: " + output
        );
    }

    @Test
    void cloudAuthDoesntRequirePortOrAuthType() {
        // The expected error message verifies that the user does not need to provide a port when auth-type=cloud.
        // Otherwise, the validation for a missing port would trigger and no connection attempt would be made.
        // Enhanced in 2.0.0 to not require authType either. We can assume that if the user specifies a cloud-api-key,
        // then port should be 443 and auth type should be cloud.
        assertStderrContains(
            "Unable to call token endpoint at https://localhost/token",
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--host", "localhost",
            "--cloud-api-key", "doesnt-matter-for-this-test"
        );
    }
}
