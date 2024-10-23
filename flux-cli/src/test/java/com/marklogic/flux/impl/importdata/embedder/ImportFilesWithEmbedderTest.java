/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata.embedder;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

public class ImportFilesWithEmbedderTest extends AbstractTest {

    @Test
    void test() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/json-files,''",
            "--splitter-json-pointer", "/text",
            "--splitter-max-chunk-size", "500",
            "--splitter-output-max-chunks", "2",
            "--embedder", "minilm"
        );

        System.out.println(readJsonDocument("/java-client-intro.json-chunks-0.json").toPrettyString());
    }
}
