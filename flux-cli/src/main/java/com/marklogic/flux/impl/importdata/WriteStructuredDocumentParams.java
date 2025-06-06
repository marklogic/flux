/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.ConnectionParams;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.tde.*;
import com.marklogic.spark.ContextSupport;
import com.marklogic.spark.Options;
import com.marklogic.spark.Util;
import marklogicspark.marklogic.client.DatabaseClient;
import org.apache.spark.sql.types.StructType;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;

/**
 * For import commands that can write "structured" rows with an arbitrary schema, either as JSON or XML documents.
 */
public class WriteStructuredDocumentParams extends WriteDocumentParams<WriteStructuredDocumentsOptions> implements WriteStructuredDocumentsOptions {

    @CommandLine.Option(
        names = "--json-root-name",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @CommandLine.Option(
        names = "--xml-root-name",
        description = "Causes XML documents to be written instead of JSON, with each document having a root element with this name."
    )
    private String xmlRootName;

    @CommandLine.Option(
        names = "--xml-namespace",
        description = "Namespace for the root element of XML documents as specified by '--xml-root-name'."
    )
    private String xmlNamespace;

    @CommandLine.Option(
        names = "--ignore-null-fields",
        description = "Ignore fields with null values in the data source when writing JSON or XML documents to MarkLogic."
    )
    private boolean ignoreNullFields;

    @CommandLine.Option(
        names = "--tde-schema",
        description = "Schema name of the TDE template to generate. Has no effect unless --tde-view is also specified."

    )
    private String tdeSchema;

    @CommandLine.Option(
        names = "--tde-view",
        description = "View name of the TDE template to generate. Has no effect unless --tde-schema is also specified."
    )
    private String tdeView;

    @CommandLine.Option(
        names = "--tde-document-type",
        description = "Type of document to generate in the TDE template; defaults to 'json', can be set to 'xml'."
    )
    private String tdeDocumentType = "json";

    @CommandLine.Option(
        names = "--tde-preview"
    )
    private boolean tdePreview;

    @CommandLine.Option(
        names = "--tde-permissions",
        description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to the generated TDE template - e.g. role1,read,role2,update,role3,execute."
    )
    private String tdePermissions;

    @CommandLine.Option(
        names = "--tde-collections",
        description = "Comma-delimited sequence of collection names to include in the generated TDE template - e.g. collection1,collection2."
    )
    private String tdeCollections;

    @CommandLine.Option(
        names = "--tde-directory",
        description = "Database directories to include in the generated TDE template."
    )
    private List<String> tdeDirectories;

    @CommandLine.Option(
        names = "--tde-context",
        description = "Context path to use for the generated TDE template. Will override the default context path of " +
            "'/' and any context path determined by the use of --json-root-name or --xml-root-name."
    )
    private String tdeContext;

    @CommandLine.Option(
        names = "--tde-uri",
        description = "URI for loading the generated TDE template."
    )
    private String tdeUri;

    @CommandLine.Option(
        names = "--tde-template-disabled",
        description = "If set, the TDE template will be loaded but in a disabled state."
    )
    private boolean tdeTemplateDisabled;

    @CommandLine.Option(
        names = "--tde-view-layout",
        description = "View layout for the TDE template; defaults to 'sparse', can instead be set to 'identical'."
    )
    private String tdeViewLayout;

    @CommandLine.Option(
        names = "--tde-column-val",
        description = "Custom 'val' value for a column name in the generated TDE template; e.g. --tde-column-val myColumn=myValue. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnVals;

    @CommandLine.Option(
        names = "--tde-column-type",
        description = "Custom scalar type for a column name in the generated TDE template; e.g. --tde-column-type myColumn=dateTime. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnTypes;

    @CommandLine.Option(
        names = "--tde-column-default",
        description = "Default value for a column name in the generated TDE template; e.g. --tde-column-default myColumn=defaultValue. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnDefaultValues;

    @CommandLine.Option(
        names = "--tde-column-invalid-values",
        description = "Invalid values handling for a column name in the generated TDE template; e.g. --tde-column-invalid-values myColumn=reject. " +
            "Value can be 'ignore' or 'reject'. This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnInvalidValues;

    @CommandLine.Option(
        names = "--tde-column-reindexing",
        description = "Reindexing setting for a column name in the generated TDE template; e.g. --tde-column-reindexing myColumn=visible. " +
            "Value can be 'hidden' or 'visible'. This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnReindexing;

    @CommandLine.Option(
        names = "--tde-column-permissions",
        description = "Permissions for a column name in the generated TDE template; e.g. --tde-column-permissions myColumn=role1,read,role2,update. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnPermissions;

    @CommandLine.Option(
        names = "--tde-column-nullable",
        description = "Name of a column that should be marked as nullable in the generated TDE template. " +
            "This option can be specified multiple times."
    )
    private List<String> tdeNullableColumns;

    @CommandLine.Option(
        names = "--tde-column-collation",
        description = "Collation for a column name in the generated TDE template; e.g. --tde-column-collation myColumn=http://marklogic.com/collation/codepoint. " +
            "This option can be specified multiple times."
    )
    private Map<String, String> tdeColumnCollation;

    @Override
    public Map<String, String> makeOptions() {
        Map<String, String> options = super.makeOptions();
        if (ignoreNullFields) {
            options.put(Options.WRITE_JSON_SERIALIZATION_OPTION_PREFIX + "ignoreNullFields", "true");
        }
        return OptionsUtil.addOptions(options,
            Options.WRITE_JSON_ROOT_NAME, jsonRootName,
            Options.WRITE_XML_ROOT_NAME, xmlRootName,
            Options.WRITE_XML_NAMESPACE, xmlNamespace
        );
    }

    /**
     * @param sparkSchema
     * @param connectionParams
     * @return true if TDE was generated and previewed, false otherwise. This will likely change once we
     * support loading the TDE into MarkLogic
     * <p>
     * Pretty sure this logic will move before all the stories for the feature are done. The params in this class can
     * be used to construct a TdeInputs instance, and the rest of everything can hopefully happen outside of the
     * command/params hierarchy as opposed to happening here.
     */
    public boolean generateTde(StructType sparkSchema, ConnectionParams connectionParams) {
        if (tdeSchema != null && tdeView != null) {
            TdeInputs inputs = buildTdeInputs(sparkSchema);
            TdeBuilder tdeBuilder = "xml".equalsIgnoreCase(tdeDocumentType) ? new XmlTdeBuilder() : new JsonTdeBuilder();
            TdeTemplate tdeTemplate = tdeBuilder.buildTde(inputs);
            if (tdePreview) {
                if (Util.MAIN_LOGGER.isInfoEnabled()) {
                    Util.MAIN_LOGGER.info("Generated TDE:\n{}", tdeTemplate.toPrettyString());
                }
                return true;
            } else {
                try (DatabaseClient client = new ContextSupport(connectionParams.makeOptions()).connectToMarkLogic()) {
                    new TdeLoader(client).loadTde(tdeTemplate);
                }
            }
        }

        return false;
    }

    protected final TdeInputs buildTdeInputs(StructType sparkSchema) {
        return new TdeInputs(tdeSchema, tdeView, new SparkColumnIterator(sparkSchema))
            .withUri(tdeUri)
            .withDisabled(tdeTemplateDisabled)
            .withPermissions(tdePermissions)
            .withCollections(tdeCollections != null ? tdeCollections.split(",") : null)
            .withDirectories(tdeDirectories != null ? tdeDirectories.toArray(new String[0]) : null)
            .withContext(tdeContext)
            .withJsonRootName(jsonRootName)
            .withXmlRootName(xmlRootName, xmlNamespace)
            .withViewLayout(tdeViewLayout)
            .withColumnVals(tdeColumnVals)
            .withColumnTypes(tdeColumnTypes)
            .withColumnDefaultValues(tdeColumnDefaultValues)
            .withColumnInvalidValues(tdeColumnInvalidValues)
            .withColumnReindexing(tdeColumnReindexing)
            .withColumnPermissions(tdeColumnPermissions)
            .withColumnCollations(tdeColumnCollation)
            .withNullableColumns(tdeNullableColumns);
    }

    @Override
    public WriteStructuredDocumentsOptions jsonRootName(String jsonRootName) {
        this.jsonRootName = jsonRootName;
        return this;
    }

    @Override
    public WriteStructuredDocumentsOptions xmlRootName(String xmlRootName) {
        this.xmlRootName = xmlRootName;
        return this;
    }

    @Override
    public WriteStructuredDocumentsOptions xmlNamespace(String xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
        return this;
    }

    @Override
    public WriteStructuredDocumentsOptions ignoreNullFields(boolean value) {
        this.ignoreNullFields = value;
        return this;
    }
}
