/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.tde.TdeInputs;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void tdeColumnCustomizations() {
        ImportDelimitedFilesCommand command = (ImportDelimitedFilesCommand) getCommand(
            "import-delimited-files",
            "--path", "anywhere",
            "--tde-schema", "test-schema",
            "--tde-view", "test-view",
            "--tde-column-val", "column1=customVal1",
            "--tde-column-val", "column2=customVal2",
            "--tde-column-type", "column1=dateTime",
            "--tde-column-type", "column2=int",
            "--tde-column-default", "column1=defaultValue1",
            "--tde-column-default", "column2=defaultValue2",
            "--tde-column-invalid-values", "column1=reject",
            "--tde-column-invalid-values", "column2=ignore",
            "--tde-column-reindexing", "column1=visible",
            "--tde-column-reindexing", "column2=hidden",
            "--tde-column-permissions", "column1=role1,role2",
            "--tde-column-permissions", "column2=role3",
            "--tde-column-nullable", "column1",
            "--tde-column-nullable", "column2",
            "--tde-column-collation", "column1=http://marklogic.com/collation/codepoint",
            "--tde-column-collation", "column2=http://marklogic.com/collation/"
        );

        WriteStructuredDocumentParams params = (WriteStructuredDocumentParams) command.getWriteParams();
        StructType schema = new StructType()
            .add("column1", DataTypes.StringType)
            .add("column2", DataTypes.StringType);
        TdeInputs inputs = params.buildTdeInputs(schema);

        Map<String, String> columnVals = inputs.getColumnVals();
        assertEquals("customVal1", columnVals.get("column1"));
        assertEquals("customVal2", columnVals.get("column2"));

        Map<String, String> columnTypes = inputs.getColumnTypes();
        assertEquals("dateTime", columnTypes.get("column1"));
        assertEquals("int", columnTypes.get("column2"));

        Map<String, String> columnDefaultValues = inputs.getColumnDefaultValues();
        assertEquals("defaultValue1", columnDefaultValues.get("column1"));
        assertEquals("defaultValue2", columnDefaultValues.get("column2"));

        Map<String, String> columnInvalidValues = inputs.getColumnInvalidValues();
        assertEquals("reject", columnInvalidValues.get("column1"));
        assertEquals("ignore", columnInvalidValues.get("column2"));

        Map<String, String> columnReindexing = inputs.getColumnReindexing();
        assertEquals("visible", columnReindexing.get("column1"));
        assertEquals("hidden", columnReindexing.get("column2"));

        Map<String, String> columnPermissions = inputs.getColumnPermissions();
        assertEquals("role1,role2", columnPermissions.get("column1"));
        assertEquals("role3", columnPermissions.get("column2"));

        Map<String, String> columnCollation = inputs.getColumnCollations();
        assertEquals("http://marklogic.com/collation/codepoint", columnCollation.get("column1"));
        assertEquals("http://marklogic.com/collation/", columnCollation.get("column2"));

        List<String> nullableColumns = inputs.getNullableColumns();
        assertEquals(2, nullableColumns.size());
        assertTrue(nullableColumns.contains("column1"));
        assertTrue(nullableColumns.contains("column2"));
    }
}
