/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportRdfFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportRdfFilesCommand command = (ImportRdfFilesCommand) getCommand(
            "import-rdf-files",
            "--path", "src/test/resources/rdf",
            "--preview", "10",
            "--partitions", "4"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "4"
        );
    }
}
