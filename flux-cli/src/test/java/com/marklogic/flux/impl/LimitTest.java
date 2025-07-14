/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

/**
 * Can add more tests to this for other commands where we want to ensure that the limit is pushed down
 * for optimization reasons, though that will typically only be verifiable via inspection of logging.
 */
class LimitTest extends AbstractTest {

    private static final String COLLECTION = "limit-files";

    @Test
    void importFiles() {
        importFiles(2);
        assertCollectionSize(COLLECTION, 2);

        importFiles(200);
        assertCollectionSize("Only the 4 matching files should have been selected", COLLECTION, 4);

        importFiles(0);
        assertCollectionSize("Interestingly, asking Spark to apply a limit of zero results in the " +
            "limit being ignored", COLLECTION, 4);
    }

    private void importFiles(int limit) {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", COLLECTION,
            "--limit", Integer.toString(limit)
        );
    }
}
