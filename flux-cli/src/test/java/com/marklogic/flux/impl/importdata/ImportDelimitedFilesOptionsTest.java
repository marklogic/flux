/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.Flux;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.tde.TdeInputs;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
        TdeInputs inputs = params.newTdeHelper().buildTdeInputs();
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
        TdeInputs inputs = params.newTdeHelper().buildTdeInputs();

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

        Map<String, Set<String>> columnPermissions = inputs.getColumnPermissions();
        assertTrue(columnPermissions.get("column1").contains("role1"));
        assertTrue(columnPermissions.get("column1").contains("role2"));
        assertTrue(columnPermissions.get("column2").contains("role3"));

        Map<String, String> columnCollation = inputs.getColumnCollations();
        assertEquals("http://marklogic.com/collation/codepoint", columnCollation.get("column1"));
        assertEquals("http://marklogic.com/collation/", columnCollation.get("column2"));

        List<String> nullableColumns = inputs.getNullableColumns();
        assertEquals(2, nullableColumns.size());
        assertTrue(nullableColumns.contains("column1"));
        assertTrue(nullableColumns.contains("column2"));
    }

    @Test
    void tdeOptionsFromApi() {
        AtomicReference<WriteStructuredDocumentParams> writeParams = new AtomicReference<>();
        AtomicReference<TdeParams> tdeParams = new AtomicReference<>();

        Flux.importDelimitedFiles()
            .from("src/test/resources/delimited-files/three-rows.csv")
            .to(options -> {
                writeParams.set((WriteStructuredDocumentParams) options);
                options.tdeOptions(tdeOptions -> {
                    tdeParams.set((TdeParams) tdeOptions);
                    tdeOptions
                        .schemaName("api-schema")
                        .viewName("api-view")
                        .documentType("xml")
                        .preview()
                        .permissions("role1,read,role2,update")
                        .collections("collection1,collection2")
                        .directories(List.of("/dir1", "/dir2"))
                        .context("/custom-context")
                        .uri("/custom/uri.xml")
                        .templateDisabled()
                        .viewVirtual()
                        .viewLayout("identical")
                        .columnVals(Map.of("col1", "val1"))
                        .columnTypes(Map.of("col1", "string"))
                        .columnDefaultValues(Map.of("col1", "default"))
                        .columnInvalidValues(Map.of("col1", "ignore"))
                        .columnReindexing(Map.of("col1", "hidden"))
                        .columnPermissions(Map.of("col1", "role1,role2"))
                        .nullableColumns(List.of("col1"))
                        .columnCollations(Map.of("col1", "http://marklogic.com/collation/"));
                });
            });

        WriteStructuredDocumentParams params = writeParams.get();
        TdeInputs inputs = params.newTdeHelper().buildTdeInputs();

        // Verify all TdeOptions methods were applied correctly
        assertEquals("api-schema", inputs.getSchemaName());
        assertEquals("api-view", inputs.getViewName());
        assertEquals("/custom-context", inputs.getContext());
        assertEquals("/custom/uri.xml", inputs.getUri());
        assertTrue(inputs.isDisabled());
        assertTrue(inputs.isViewVirtual());
        assertEquals("identical", inputs.getViewLayout());
        assertEquals("role1,read,role2,update", inputs.getPermissions());

        assertEquals(2, inputs.getCollections().length);
        assertEquals("collection1", inputs.getCollections()[0]);
        assertEquals("collection2", inputs.getCollections()[1]);

        assertEquals(2, inputs.getDirectories().length);
        assertEquals("/dir1", inputs.getDirectories()[0]);
        assertEquals("/dir2", inputs.getDirectories()[1]);

        assertEquals("val1", inputs.getColumnVals().get("col1"));
        assertEquals("string", inputs.getColumnTypes().get("col1"));
        assertEquals("default", inputs.getColumnDefaultValues().get("col1"));
        assertEquals("ignore", inputs.getColumnInvalidValues().get("col1"));
        assertEquals("hidden", inputs.getColumnReindexing().get("col1"));
        assertTrue(inputs.getColumnPermissions().get("col1").contains("role1"));
        assertTrue(inputs.getColumnPermissions().get("col1").contains("role2"));
        assertTrue(inputs.getNullableColumns().contains("col1"));
        assertEquals("http://marklogic.com/collation/", inputs.getColumnCollations().get("col1"));

        // Verify TdeParams-specific fields
        assertEquals("xml", tdeParams.get().getDocumentType());
        assertTrue(tdeParams.get().isPreview());
    }

    @Test
    void tdeOptionsFromApiForVectorColumn() {
        AtomicReference<WriteStructuredDocumentParams> writeParams = new AtomicReference<>();
        AtomicReference<TdeParams> tdeParams = new AtomicReference<>();

        Flux.importDelimitedFiles()
            .from("src/test/resources/delimited-files/three-rows.csv")
            .to(options -> {
                writeParams.set((WriteStructuredDocumentParams) options);
                options.tdeOptions(tdeOptions -> {
                    tdeParams.set((TdeParams) tdeOptions);
                    tdeOptions
                        .schemaName("api-schema")
                        .viewName("api-view")
                        .preview()
                        .context("/custom-context")
                        .virtualColumns(List.of("col1"))
                        .columnTypes(Map.of("col1", "vector"))
                        .columnDimensions(Map.of("col1", 384))
                        .columnAnnCompressions(Map.of("col1", 0.5f))
                        .columnAnnDistances(Map.of("col1", "cosine"))
                        .columnAnnIndexed(Map.of("col1", true));
                });
            });

        WriteStructuredDocumentParams params = writeParams.get();
        TdeInputs inputs = params.newTdeHelper().buildTdeInputs();

        // Verify all TdeOptions methods were applied correctly
        assertEquals("api-schema", inputs.getSchemaName());
        assertEquals("api-view", inputs.getViewName());
        assertEquals("/custom-context", inputs.getContext());
        assertEquals("vector", inputs.getColumnTypes().get("col1"));

        assertEquals("col1", inputs.getVirtualColumns().get(0));
        assertEquals(1, inputs.getVirtualColumns().size());
        
        assertEquals(384, inputs.getColumnDimensions().get("col1").intValue());
        assertEquals(0.5f, inputs.getColumnAnnCompressions().get("col1").floatValue());
        assertEquals("cosine", inputs.getColumnAnnDistances().get("col1"));
        assertTrue(inputs.getColumnAnnIndexed().get("col1"));
    }
}
