/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportAggregateJsonFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportAggregateJsonFilesCommand command = (ImportAggregateJsonFilesCommand) getCommand(
            "import-aggregate-json-files",
            "--path", "anywhere",
            "--encoding", "UTF-16"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertEquals("UTF-16", options.get("encoding"), "The --encoding option is a convenience for specifying " +
            "the Spark JSON option so the user doesn't have to also learn -Pencoding=");
    }
}
