/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class ShowSparkProgressBarTest extends AbstractTest {

    @Test
    void test() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--filter", "hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "test-files",
            "--spark-show-progress-bar"
        );

        assertCollectionSize(
            "This test ensures that include --spark-show-progress-bar doesn't cause any errors.",
            "test-files", 4
        );
    }
}
