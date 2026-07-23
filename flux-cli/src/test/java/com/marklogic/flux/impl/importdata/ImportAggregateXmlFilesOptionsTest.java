/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImportAggregateXmlFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportAggregateXmlFilesCommand command = (ImportAggregateXmlFilesCommand) getCommand(
            "import-aggregate-xml-files",
            "--connection-string", makeConnectionString(),
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

    @Test
    void zipProtectionOptions() {
        ImportAggregateXmlFilesCommand command = (ImportAggregateXmlFilesCommand) getCommand(
            "import-aggregate-xml-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources",
            "--element", "anything",
            "--zip-max-entry-bytes", "268435456",
            "--zip-max-entry-count", "100000"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES, "268435456",
            Options.READ_ZIP_MAX_ENTRY_COUNT, "100000"
        );
    }

    @Test
    void zipProtectionOptionsNotSetByDefault() {
        ImportAggregateXmlFilesCommand command = (ImportAggregateXmlFilesCommand) getCommand(
            "import-aggregate-xml-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources",
            "--element", "anything"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES),
            "Zip byte limit should not be set when the flag is omitted.");
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_ENTRY_COUNT),
            "Zip entry count limit should not be set when the flag is omitted.");
    }

    @Test
    void zipZeroAndNegativeValuesAreIgnored() {
        ImportAggregateXmlFilesCommand command = (ImportAggregateXmlFilesCommand) getCommand(
            "import-aggregate-xml-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources",
            "--element", "anything",
            "--zip-max-entry-bytes", "0",
            "--zip-max-entry-count", "-1"
        );
        Map<String, String> options = command.getReadParams().makeOptions();
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES));
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_ENTRY_COUNT));
    }
}
