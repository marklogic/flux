/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportArchiveFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--preview", "10",
            "--partitions", "18",
            "--encoding", "UTF-16"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "18",
            Options.READ_FILES_ENCODING, "UTF-16"
        );
    }

    @Test
    void streaming() {
        ImportArchiveFilesCommand command = (ImportArchiveFilesCommand) getCommand(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--streaming",
            "--document-type", "xml"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.STREAM_FILES, "true",
            Options.READ_FILES_TYPE, "archive"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.STREAM_FILES, "true",
            Options.WRITE_DOCUMENT_TYPE, "XML"
        );
    }
}
