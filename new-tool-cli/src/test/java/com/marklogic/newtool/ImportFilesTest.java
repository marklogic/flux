package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

public class ImportFilesTest extends AbstractTest {

    @Test
    void csv() {
        run(
            "import_files",
            "--format", "csv",
            "--path", "src/test/resources/data.csv",
            "-R:header=true",
            "--uri-template", "/doc/{docNum}.json"
        );
    }
}
