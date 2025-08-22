/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportJdbcOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ExportJdbcCommand command = (ExportJdbcCommand) getCommand("export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", "anything",
            "--partitions", "12",
            "--jdbc-url", "some-jdbc-url",
            "--table", "anywhere"
        );

        assertEquals("local[12]", command.determineSparkMasterUrl());
    }

    @Test
    void userDefinedSparkMasterUrl() {
        ExportJdbcCommand command = (ExportJdbcCommand) getCommand("export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", "anything",
            "--partitions", "12",
            "--spark-master-url", "local[6]",
            "--jdbc-url", "some-jdbc-url",
            "--table", "anywhere"
        );

        assertEquals("local[6]", command.determineSparkMasterUrl(), "If the user defines the Spark master URL, " +
            "then it should be used instead of the one calculated from the number of partitions.");
    }
}
