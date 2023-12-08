package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

public class ImportJsonTest extends AbstractTest {

    @Test
    void jsonLines() {
        run(
            "import_json",
            "--path", "src/test/resources/json-lines",
            "--jsonLines",
            "--preview"
        );
    }

    @Test
    void multiLineJson() {
        run(
            "import_json",
            "--path", "src/test/resources/json-files",
            "--preview"
        );
    }
}
