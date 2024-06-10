package com.marklogic.newtool.impl.importdata;

import com.marklogic.newtool.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void numPartitions() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--preview", "10",
            "--partitions", "18"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "18"
        );
    }
}
