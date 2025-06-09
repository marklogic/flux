/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.tde.TdeInputs;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Extends the non-Spark-specific TdeInputs class to provide command-line options for each of the inputs. The document
 * type and preview mode are specific to Flux and thus not part of the TdeInputs class.
 */
public class TdeParams extends TdeInputs {

    private String documentType = "json";
    private boolean preview;

    @CommandLine.Option(
        names = "--tde-schema",
        description = "Schema name of the TDE template to generate. Has no effect unless --tde-view is also specified."
    )
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @CommandLine.Option(
        names = "--tde-view",
        description = "View name of the TDE template to generate. Has no effect unless --tde-schema is also specified."
    )
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @CommandLine.Option(
        names = "--tde-context",
        description = "Context path to use for the generated TDE template. Will override the default context path of " +
            "'/' and any context path determined by the use of --json-root-name or --xml-root-name."
    )
    public void setContext(String context) {
        this.context = context;
    }

    @CommandLine.Option(
        names = "--tde-collections",
        description = "Comma-delimited sequence of collection names to include in the generated TDE template - e.g. collection1,collection2."
    )
    public void setCollectionsString(String collectionsString) {
        this.collections = collectionsString != null ? collectionsString.split(",") : null;
    }

    @CommandLine.Option(
        names = "--tde-directory",
        description = "Database directories to include in the generated TDE template."
    )
    public void setDirectoriesList(List<String> directories) {
        this.directories = directories != null ? directories.toArray(new String[0]) : null;
    }

    @CommandLine.Option(
        names = "--tde-permissions",
        description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to the generated TDE template - e.g. role1,read,role2,update,role3,execute."
    )
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @CommandLine.Option(
        names = "--tde-uri",
        description = "URI for loading the generated TDE template."
    )
    public void setUri(String uri) {
        this.uri = uri;
    }

    @CommandLine.Option(
        names = "--tde-template-disabled",
        description = "If set, the TDE template will be loaded but in a disabled state."
    )
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @CommandLine.Option(
        names = "--tde-view-layout",
        description = "View layout for the TDE template; defaults to 'sparse', can instead be set to 'identical'."
    )
    public void setViewLayout(String viewLayout) {
        this.viewLayout = viewLayout;
    }

    @CommandLine.Option(
        names = "--tde-column-val",
        description = "Custom 'val' value for a column name in the generated TDE template; e.g. --tde-column-val myColumn=myValue. " +
            "This option can be specified multiple times."
    )
    public void setColumnVals(Map<String, String> columnVals) {
        this.columnVals = columnVals;
    }

    @CommandLine.Option(
        names = "--tde-column-type",
        description = "Custom scalar type for a column name in the generated TDE template; e.g. --tde-column-type myColumn=dateTime. " +
            "This option can be specified multiple times."
    )
    public void setColumnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    @CommandLine.Option(
        names = "--tde-column-default",
        description = "Default value for a column name in the generated TDE template; e.g. --tde-column-default myColumn=defaultValue. " +
            "This option can be specified multiple times."
    )
    public void setColumnDefaultValues(Map<String, String> columnDefaultValues) {
        this.columnDefaultValues = columnDefaultValues;
    }

    @CommandLine.Option(
        names = "--tde-column-invalid-values",
        description = "Invalid values handling for a column name in the generated TDE template; e.g. --tde-column-invalid-values myColumn=reject. " +
            "Value can be 'ignore' or 'reject'. This option can be specified multiple times."
    )
    public void setColumnInvalidValues(Map<String, String> columnInvalidValues) {
        this.columnInvalidValues = columnInvalidValues;
    }

    @CommandLine.Option(
        names = "--tde-column-reindexing",
        description = "Reindexing setting for a column name in the generated TDE template; e.g. --tde-column-reindexing myColumn=visible. " +
            "Value can be 'hidden' or 'visible'. This option can be specified multiple times."
    )
    public void setColumnReindexing(Map<String, String> columnReindexing) {
        this.columnReindexing = columnReindexing;
    }

    @CommandLine.Option(
        names = "--tde-column-permissions",
        description = "Comma-delimited role names for defining read permissions for a column in the generated " +
            "TDE template; e.g. --tde-column-permissions myColumn=role1,role2. " +
            "This option can be specified multiple times."
    )
    public void setColumnPermissionsString(Map<String, String> columnPermissionsString) {
        if (columnPermissionsString != null) {
            this.columnPermissions = columnPermissionsString.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Arrays.stream(entry.getValue().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet())
                ));
        }
    }

    @CommandLine.Option(
        names = "--tde-column-nullable",
        description = "Name of a column that should be marked as nullable in the generated TDE template. " +
            "This option can be specified multiple times."
    )
    public void setNullableColumns(List<String> nullableColumns) {
        this.nullableColumns = nullableColumns;
    }

    @CommandLine.Option(
        names = "--tde-column-collation",
        description = "Collation for a column name in the generated TDE template; e.g. --tde-column-collation myColumn=http://marklogic.com/collation/codepoint. " +
            "This option can be specified multiple times."
    )
    public void setColumnCollations(Map<String, String> columnCollations) {
        this.columnCollations = columnCollations;
    }

    @CommandLine.Option(
        names = "--tde-document-type",
        description = "Type of document to generate in the TDE template; defaults to 'json', can be set to 'xml'."
    )
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @CommandLine.Option(
        names = "--tde-preview"
    )
    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public String getDocumentType() {
        return documentType;
    }

    public boolean isPreview() {
        return preview;
    }

}
