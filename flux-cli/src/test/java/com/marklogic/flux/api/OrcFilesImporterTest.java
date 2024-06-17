/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrcFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importOrcFiles()
            .connectionString(makeConnectionString())
            .from("src/test/resources/orc-files/authors.orc")
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test")
                .uriTemplate("/orc-test/{LastName}.json"))
            .execute();

        assertCollectionSize("orc-test", 15);
    }

    @Test
    void badOption() {
        OrcFilesImporter importer = Flux.importOrcFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/orc-files/authors.orc")
                .additionalOptions(Map.of("mergeSchema", "not-valid-value"))
            )
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test"));

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("For input string: \"not-valid-value\"", ex.getMessage(), "Expecting a failure due to the " +
            "invalid value for the ORC 'mergeSchema' option.");
    }

    @Test
    void missingPath() {
        OrcFilesImporter importer = Flux.importOrcFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
