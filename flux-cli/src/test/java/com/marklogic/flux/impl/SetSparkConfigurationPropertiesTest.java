/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SetSparkConfigurationPropertiesTest extends AbstractTest {

    @Test
    void validSparkConfProperty() {
        run(
            "import-files",
            "-Cspark.io.encryption.enabled=true",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        assertCollectionSize(
            "The import should succeed because the -C option has a valid Spark configuration property.",
            "files", 4);
    }

    @Test
    void invalidSparkConfProperty() {
        String stderr = runAndReturnStderr(
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "-Cspark.io.encryption.enabled=invalid",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        assertTrue(stderr.contains("The value 'invalid' in the config \"spark.io.encryption.enabled\" is invalid"),
            "Unexpected stderr: " + stderr);

        assertCollectionSize("The operation should have failed immediately due to the invalid value for a " +
            "Spark Session builder option", "files", 0);
    }
}
