package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest extends AbstractTest {

    @Test
    void test() {
        String stdout = runAndReturnStdout(() -> run("version"));
        assertTrue(stdout.contains("Flux version:"), "Unexpected stdout: " + stdout);
    }

    @Test
    void verbose() {
        String stdout = runAndReturnStdout(() -> run("version", "--verbose"));
        assertTrue(stdout.contains("\"buildTime\""), "When using --verbose, the output should be a JSON object " +
            "that includes the build time for performance tests and can thus be easily parsed by another program; " +
            "unexpected stdout: " + stdout);
    }
}
