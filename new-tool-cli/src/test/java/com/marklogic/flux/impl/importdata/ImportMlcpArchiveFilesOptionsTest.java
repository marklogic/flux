package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportMlcpArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportMlcpArchiveFilesCommand command = (ImportMlcpArchiveFilesCommand) getCommand(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/archive-files",
            "--preview", "10",
            "--partitions", "7"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "7"
        );
    }
}
