/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportAggregateXmlFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportAggregateXmlFilesCommand command = (ImportAggregateXmlFilesCommand) getCommand(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file",
            "--preview", "10",
            "--element", "anything",
            "--partitions", "3"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_AGGREGATES_XML_ELEMENT, "anything",
            Options.READ_NUM_PARTITIONS, "3"
        );
    }
}
