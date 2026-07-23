/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImportRdfFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportRdfFilesCommand command = (ImportRdfFilesCommand) getCommand(
            "import-rdf-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/rdf",
            "--preview", "10",
            "--partitions", "4"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "4"
        );
    }

    @Test
    void zipProtectionOptions() {
        ImportRdfFilesCommand command = (ImportRdfFilesCommand) getCommand(
            "import-rdf-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/rdf",
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
        ImportRdfFilesCommand command = (ImportRdfFilesCommand) getCommand(
            "import-rdf-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/rdf"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES),
            "Zip byte limit should not be set when the flag is omitted.");
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_ENTRY_COUNT),
            "Zip entry count limit should not be set when the flag is omitted.");
    }

    @Test
    void zipZeroAndNegativeValuesAreIgnored() {
        ImportRdfFilesCommand command = (ImportRdfFilesCommand) getCommand(
            "import-rdf-files",
            "--connection-string", makeConnectionString(),
            "--path", "src/test/resources/rdf",
            "--zip-max-entry-bytes", "0",
            "--zip-max-entry-count", "-1"
        );
        Map<String, String> options = command.getReadParams().makeOptions();
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES));
        assertFalse(options.containsKey(Options.READ_ZIP_MAX_ENTRY_COUNT));
    }
}
