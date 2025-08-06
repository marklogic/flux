/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportMlcpArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportMlcpArchiveFilesCommand command = (ImportMlcpArchiveFilesCommand) getCommand(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/archive-files",
            "--preview", "10",
            "--partitions", "7",
            "--encoding", "UTF-16"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "7",
            Options.READ_FILES_ENCODING, "UTF-16"
        );
    }
}
