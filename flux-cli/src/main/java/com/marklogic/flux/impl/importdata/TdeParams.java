/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.TdeOptions;
import com.marklogic.flux.tde.TdeInputs;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TdeParams implements TdeOptions {

    @CommandLine.Option(
        names = "--tde-schema",
        description = "Schema name of the TDE template to generate. Has no effect unless --tde-view is also specified."
    )
    private String schema;

    @CommandLine.Option(
        names = "--tde-view",
        description = "View name of the TDE template to generate. Has no effect unless --tde-schema is also specified."
    )
    private String view;

    @CommandLine.Option(
        names = "--tde-document-type",
        description = "Type of document to generate in the TDE template; defaults to 'json', can be set to 'xml'."
    )
    private String documentType = "json";

    @CommandLine.Option(
        names = "--tde-preview",
        description = "If set, the TDE template will be generated but not loaded into MarkLogic. " +
            "This is useful for previewing the generated TDE template without making any changes to the database."
    )
    private boolean preview;

    @CommandLine.Option(
        names = "--tde-permissions",
        description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to the generated TDE template - e.g. role1,read,role2,update,role3,execute."
    )
    private String permissions;

    @CommandLine.Option(
        names = "--tde-collections",
        description = "Comma-delimited sequence of collection names to include in the generated TDE template - e.g. collection1,collection2."
    )
    private String collections;

    @CommandLine.Option(
        names = "--tde-directory",
        description = "Database directories to include in the generated TDE template."
    )
    private List<String> directories;

    @CommandLine.Option(
        names = "--tde-context",
        description = "Context path to use for the generated TDE template. Will override the default context path of " +
            "'/' and any context path determined by the use of --json-root-name or --xml-root-name."
    )
    private String context;

    @CommandLine.Option(
        names = "--tde-uri",
        description = "URI for loading the generated TDE template."
    )
    private String uri;

    @CommandLine.Option(
        names = "--tde-template-disabled",
        description = "If set, the TDE template will be loaded but in a disabled state."
    )
    private boolean templateDisabled;

    @CommandLine.Option(
        names = "--tde-view-virtual",
        description = "If set, the TDE template will extract content at query time instead of when data is indexed."
    )
    private boolean viewVirtual;

    @CommandLine.Option(
        names = "--tde-view-layout",
        description = "View layout for the TDE template; defaults to 'sparse', can instead be set to 'identical'."
    )
    private String viewLayout;

    @CommandLine.Option(
        names = "--tde-column-val",
        description = "Custom 'val' value for a column name in the generated TDE template; e.g. --tde-column-val myColumn=myValue. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnVals;

    @CommandLine.Option(
        names = "--tde-column-type",
        description = "Custom scalar type for a column name in the generated TDE template; e.g. --tde-column-type myColumn=dateTime. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnTypes;

    @CommandLine.Option(
        names = "--tde-column-default",
        description = "Default value for a column name in the generated TDE template; e.g. --tde-column-default myColumn=defaultValue. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnDefaultValues;

    @CommandLine.Option(
        names = "--tde-column-invalid-values",
        description = "Invalid values handling for a column name in the generated TDE template; e.g. --tde-column-invalid-values myColumn=reject. " +
            "Value can be 'ignore' or 'reject'. This option can be specified multiple times."
    )
    private Map<String, String> columnInvalidValues;

    @CommandLine.Option(
        names = "--tde-column-reindexing",
        description = "Reindexing setting for a column name in the generated TDE template; e.g. --tde-column-reindexing myColumn=visible. " +
            "Value can be 'hidden' or 'visible'. This option can be specified multiple times."
    )
    private Map<String, String> columnReindexing;

    @CommandLine.Option(
        names = "--tde-column-permissions",
        description = "Comma-delimited role names for defining read permissions for a column in the generated " +
            "TDE template; e.g. --tde-column-permissions myColumn=role1,role2. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnPermissions;

    @CommandLine.Option(
        names = "--tde-column-nullable",
        description = "Name of a column that should be marked as nullable in the generated TDE template. " +
            "This option can be specified multiple times."
    )
    private List<String> nullableColumns;

    @CommandLine.Option(
        names = "--tde-column-collation",
        description = "Collation for a column name in the generated TDE template; e.g. --tde-column-collation myColumn=http://marklogic.com/collation/codepoint. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnCollation;

    @CommandLine.Option(
        names = "--tde-column-virtual",
        description = "Name of a column that should be marked as virtual in the generated TDE template. " +
            "This option can be specified multiple times."
    )
    private List<String> virtualColumns;

    @CommandLine.Option(
        names = "--tde-column-dimension",
        description = "Vector dimension for a column in the generated TDE template; e.g. --tde-column-dimension myVectorColumn=384 . " +
            "This option can be specified multiple times."
    )
    private Map<String, Integer> columnDimensions;

    @CommandLine.Option(
        names = "--tde-column-ann-compression",
        description = "ANN compression ratio for a vector column in the generated TDE template; e.g. --tde-column-ann-compression myVectorColumn=0.5 . " +
            "This option can be specified multiple times."
    )
    private Map<String, Float> columnAnnCompressions;

    @CommandLine.Option(
        names = "--tde-column-ann-distance",
        description = "ANN distance metric for a vector column in the generated TDE template; e.g. --tde-column-ann-distance myVectorColumn=cosine . " +
            "This option can be specified multiple times."
    )
    private Map<String, String> columnAnnDistances;

    @CommandLine.Option(
        names = "--tde-column-ann-indexed",
        description = "Controls the indexing of a vector column; e.g. --tde-column-ann-indexed myVectorColumn=true. " +
            "This option can be specified multiple times."
    )
    private Map<String, Boolean> columnAnnIndexed;

    public boolean hasSchemaAndView() {
        return schema != null && view != null && !schema.isEmpty() && !view.isEmpty();
    }

    public TdeInputs buildTdeInputs(String jsonRootName, String xmlRootName, String xmlNamespace) {
        Map<String, Set<String>> permissionsMap = null;
        if (columnPermissions != null) {
            permissionsMap = columnPermissions.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Arrays.stream(entry.getValue().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet())
                ));
        }

        return new TdeInputs(schema, view)
            .withUri(uri)
            .withDisabled(templateDisabled)
            .withPermissions(permissions)
            .withCollections(collections != null ? collections.split(",") : null)
            .withDirectories(directories != null ? directories.toArray(new String[0]) : null)
            .withContext(context)
            .withJsonRootName(jsonRootName)
            .withXmlRootName(xmlRootName, xmlNamespace)
            .withViewVirtual(viewVirtual)
            .withViewLayout(viewLayout)
            .withColumnVals(columnVals)
            .withColumnTypes(columnTypes)
            .withColumnDefaultValues(columnDefaultValues)
            .withColumnInvalidValues(columnInvalidValues)
            .withColumnReindexing(columnReindexing)
            .withColumnPermissions(permissionsMap)
            .withColumnCollations(columnCollation)
            .withNullableColumns(nullableColumns)
            .withVirtualColumns(virtualColumns)
            .withColumnDimensions(columnDimensions)
            .withColumnAnnCompressions(columnAnnCompressions)
            .withColumnAnnDistances(columnAnnDistances)
            .withColumnAnnIndexed(columnAnnIndexed);
    }

    public String getDocumentType() {
        return documentType;
    }

    public boolean isPreview() {
        return preview;
    }

    @Override
    public TdeOptions schemaName(String schemaName) {
        this.schema = schemaName;
        return this;
    }

    @Override
    public TdeOptions viewName(String viewName) {
        this.view = viewName;
        return this;
    }

    @Override
    public TdeOptions documentType(String documentType) {
        this.documentType = documentType;
        return this;
    }

    @Override
    public TdeOptions preview() {
        this.preview = true;
        return this;
    }

    @Override
    public TdeOptions permissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public TdeOptions collections(String collections) {
        this.collections = collections;
        return this;
    }

    @Override
    public TdeOptions directories(List<String> directories) {
        this.directories = directories;
        return this;
    }

    @Override
    public TdeOptions context(String context) {
        this.context = context;
        return this;
    }

    @Override
    public TdeOptions uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public TdeOptions templateDisabled() {
        this.templateDisabled = true;
        return this;
    }

    @Override
    public TdeOptions viewVirtual() {
        this.viewVirtual = true;
        return this;
    }

    @Override
    public TdeOptions viewLayout(String viewLayout) {
        this.viewLayout = viewLayout;
        return this;
    }

    @Override
    public TdeOptions columnVals(Map<String, String> columnVals) {
        this.columnVals = columnVals;
        return this;
    }

    @Override
    public TdeOptions columnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
        return this;
    }

    @Override
    public TdeOptions columnDefaultValues(Map<String, String> columnDefaultValues) {
        this.columnDefaultValues = columnDefaultValues;
        return this;
    }

    @Override
    public TdeOptions columnInvalidValues(Map<String, String> columnInvalidValues) {
        this.columnInvalidValues = columnInvalidValues;
        return this;
    }

    @Override
    public TdeOptions columnReindexing(Map<String, String> columnReindexing) {
        this.columnReindexing = columnReindexing;
        return this;
    }

    @Override
    public TdeOptions columnPermissions(Map<String, String> columnPermissions) {
        this.columnPermissions = columnPermissions;
        return this;
    }

    @Override
    public TdeOptions nullableColumns(List<String> nullableColumns) {
        this.nullableColumns = nullableColumns;
        return this;
    }

    @Override
    public TdeOptions columnCollations(Map<String, String> columnCollations) {
        this.columnCollation = columnCollations;
        return this;
    }

    @Override
    public TdeOptions virtualColumns(List<String> virtualColumns) {
        this.virtualColumns = virtualColumns;
        return this;
    }

    @Override
    public TdeOptions columnDimensions(Map<String, Integer> columnDimensions) {
        this.columnDimensions = columnDimensions;
        return this;
    }

    @Override
    public TdeOptions columnAnnCompressions(Map<String, Float> columnAnnCompressions) {
        this.columnAnnCompressions = columnAnnCompressions;
        return this;
    }

    @Override
    public TdeOptions columnAnnDistances(Map<String, String> columnAnnDistances) {
        this.columnAnnDistances = columnAnnDistances;
        return this;
    }

    @Override
    public TdeOptions columnAnnIndexed(Map<String, Boolean> columnAnnIndexed) {
        this.columnAnnIndexed = columnAnnIndexed;
        return this;
    }
}
