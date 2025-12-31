/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomExportRowsOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        CustomExportRowsCommand command = (CustomExportRowsCommand) getCommand(
            "custom-export-rows",
            "--connection-string", makeConnectionString(),
            "--query", "anything",
            "--target", "xml",
            "--partitions", "4"
        );

        assertEquals("local[*]", command.determineSparkMasterUrl(),
            "As of 2.0, the default Spark master URL is not adjusted based on the number " +
                "of partitions; this was a mistake as the URL should be controlled by the number of " +
                "available core for processing partitions via tasks.");
    }
}
