/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.tde.TdeInputs;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportDelimitedFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportDelimitedFilesCommand command = (ImportDelimitedFilesCommand) getCommand(
            "import-delimited-files",
            "--path", "anywhere",
            "--encoding", "UTF-16"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertEquals("UTF-16", options.get("encoding"), "The --encoding option is a convenience for specifying " +
            "the Spark JSON option so the user doesn't have to also learn -Pencoding=");
    }

    @Test
    void tdeDirectories() {
        ImportDelimitedFilesCommand command = (ImportDelimitedFilesCommand) getCommand(
            "import-delimited-files",
            "--path", "anywhere",
            "--encoding", "UTF-16",
            "--tde-directory", "/dir1",
            "--tde-directory", "/dir2"
        );

        WriteStructuredDocumentParams params = (WriteStructuredDocumentParams) command.getWriteParams();
        TdeInputs inputs = params.buildTdeInputs(new StructType().add("doesnt-matter", DataTypes.StringType));
        String[] directories = inputs.getDirectories();
        assertEquals(2, directories.length);
        assertEquals("/dir1", directories[0]);
        assertEquals("/dir2", directories[1], "The --tde-directory option can be specified multiple times to " +
            "avoid the awkwardness of using a newline as a delimiter. Commas don't work because they are allowed in " +
            "directory paths (though we still use commas to separate collections as a convenience for users).");
    }
}
