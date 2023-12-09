package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

public class ImportOrcTest extends AbstractTest {

    @Test
    void test() {
        run(
            "export_orc",
            "--query", "op.fromView('Medical', 'Authors', '')",
            "--path", "build/orc"
        );
    }
}
