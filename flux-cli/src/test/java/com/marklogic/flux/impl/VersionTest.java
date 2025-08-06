/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest extends AbstractTest {

    @Test
    void test() {
        String stdout = runAndReturnStdout("version");
        assertTrue(stdout.contains("Flux version:"), "Unexpected stdout: " + stdout);
    }

    @Test
    void verbose() {
        String stdout = runAndReturnStdout("version", "--verbose");
        assertTrue(stdout.contains("\"buildTime\""), "When using --verbose, the output should be a JSON object " +
            "that includes the build time for performance tests and can thus be easily parsed by another program; " +
            "unexpected stdout: " + stdout);
    }
}
