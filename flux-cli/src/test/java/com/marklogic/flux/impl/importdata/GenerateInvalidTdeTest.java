/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class GenerateInvalidTdeTest extends AbstractTest {

    @Test
    void tdeWithInvalidColumnNames() {
        assertStderrContains("TDE-INVALIDTEMPLATE",
            "import-delimited-files",
            "--path", "src/test/resources/delimited-files/funky-column-names.csv",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "funky",
            "--uri-template", "/funky/{id}.json",
            "--tde-schema", "junit",
            "--tde-view", "funky",
            "--tde-collections", "funky"
        );

        assertCollectionSize("No data should have been loaded as the single quote and forward slash " +
            "in the column names should cause an error when the TDE is loaded. We may enhance this soon " +
            "to sanitize the column name, this is just capturing the current behavior.", "funky", 0);
    }

    @Test
    void invalidIncrementalHashName() {
        assertStderrContains("TDE-INVALIDTEMPLATE",
            "import-delimited-files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "invalid",
            "--tde-schema", "junit",
            "--tde-view", "invalid",
            "--tde-collections", "invalid",
            "--incremental-write",
            "--tde-support-incremental-write",
            "--incremental-write-hash-name", "'); console.log('injection!');"
        );

        assertCollectionSize(
            "No data should have been loaded since the TDE template failed to load. This shows how any " +
                "attempt at an 'XQuery injection' attack won't work because MarkLogic will reject " +
                "the column name due to it having invalid characters in it.",
            "invalid", 0);
    }
}
