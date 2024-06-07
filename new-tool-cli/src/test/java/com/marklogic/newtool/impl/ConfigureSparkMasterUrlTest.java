package com.marklogic.newtool.impl;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Orders the tests to ensure that the invalid test runs second, which verifies that the Spark session from the first
 * test is closed. If that doesn't occur, the invalid Spark master URL in the second test will be ignored as Spark
 * will use the active session that it finds.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigureSparkMasterUrlTest extends AbstractTest {

    /**
     * Provides a simple example of setting the Spark master URL. This is intended for advanced users who want to
     * change the Spark master URL but don't want to use Spark Submit to submit a job to their own Spark cluster.
     */
    @Test
    @Order(1)
    void validMasterUrl() {
        run(
            "import-parquet-files",
            "--master-url", "local[2]",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test"
        );

        assertCollectionSize("parquet-test", 32);
    }

    @Test
    @Order(2)
    void invalidMasterUrl() {
        assertStderrContains(() -> run(
            "import-parquet-files",
            "--master-url", "just-not-valid-at-all",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        ), "Command failed, cause: Could not parse Master URL: 'just-not-valid-at-all'");
    }
}
