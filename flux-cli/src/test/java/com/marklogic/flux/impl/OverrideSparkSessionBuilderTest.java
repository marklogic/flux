/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class OverrideSparkSessionBuilderTest extends AbstractTest {

    @Test
    void hello() {
        run(
            "export-files",
            "--host", "changeme",
            "--path", "temp",
            "--base-path", "/ml/ml12/default/ragplus",
            "--auth-type", "cloud",
            "--cloud-api-key", "-Bsomething",
            "--count"
        );
    }

    @Test
    void validBuilderOption() {
        run(
            "import-files",
            "-Bspark.io.encryption.enabled=true",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        assertCollectionSize("files", 4);
    }

    @Test
    void invalidBuilderOption() {
        run(
            CommandLine.ExitCode.SOFTWARE,
            "import-files",
            "-Bspark.io.encryption.enabled=invalid",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        assertCollectionSize("The operation should have failed immediately due to the invalid value for a " +
            "Spark Session builder option", "files", 0);
    }
}
