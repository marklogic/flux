/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideSparkConfTest extends AbstractTest {

    /**
     * Verifies that the default spark.sql.session.timeZone of UTC that is set via SparkUtil can still be overridden
     * via the "-C" option.
     */
    @Test
    void overriddenTimeZone() {
        String stderr = runAndReturnStderr(
            "import-orc-files",
            "--connection-string", makeConnectionString(),
            "-Cspark.sql.orc.impl=invalid",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--preview", "1"
        );

        assertTrue(stderr.contains("INVALID_CONF_VALUE.OUT_OF_RANGE_OF_OPTIONS"),
            "This test confirms that the -C option works by passing in an invalid configuration value and " +
                "verifying that the command fails with an error message from the Spark ORC data source; " +
                "actual stderr: " + stderr);
    }

}
