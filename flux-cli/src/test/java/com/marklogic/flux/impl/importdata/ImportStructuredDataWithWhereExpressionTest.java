/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportStructuredDataWithWhereExpressionTest extends AbstractTest {

    @Test
    void validWhereClause() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--uri-template", "/car/{model}.json",
            "--where", "mpg > 30 and gear < 10"
        );

        // --where should result in 4 out of 32 cars being selected.
        List<String> uris = getUrisInCollection("parquet-test", 4);
        Stream.of("/car/Fiat 128.json", "/car/Honda Civic.json", "/car/Toyota Corolla.json", "/car/Lotus Europa.json")
            .forEach(expectedUri -> assertTrue(uris.contains(expectedUri),
                "Expected URI: " + expectedUri + "; Actual URIs: " + uris));
    }

    /**
     * Verifies that some helpful context is provided for an invalid "--where" clause, as the Spark SQL
     * error by itself can be a bit esoteric.
     */
    @Test
    void invalidWhereClause() {
        assertStderrContains("Unable to apply 'where' clause: 'this isn't valid'; cause",
            CommandLine.ExitCode.SOFTWARE,
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--uri-template", "/car/{model}.json",
            "--where", "this isn't valid"
        );
    }
}
