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
        names = "--tde-schema"
    )
    private String tdeSchema;

    @CommandLine.Option(
        names = "--tde-view"
    )
    private String tdeView;

    @CommandLine.Option(
        names = "--tde-preview"
    )
    private boolean tdePreview;

    @CommandLine.Option(
        names = "--tde-permissions",
        description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to the generated TDE template - e.g. role1,read,role2,update,role3,execute."
    )
    private String tdePermissions;

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
            TdeInputs inputs = new TdeInputs(tdeSchema, tdeView, new SparkColumnIterator(sparkSchema))
                .withPermissions(tdePermissions)
                .withJsonRootName(jsonRootName)
                .withXmlRootName(xmlRootName, xmlNamespace);

            if (tdePreview) {
                if (Util.MAIN_LOGGER.isInfoEnabled()) {
                    TdeTemplate tdeTemplate = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs);
                    Util.MAIN_LOGGER.info("Generated TDE:\n{}", tdeTemplate.toPrettyString());
                }
                return true;
            } else {
                try (DatabaseClient client = new ContextSupport(connectionParams.makeOptions()).connectToMarkLogic()) {
                    new TdeLoader(client).loadTde(inputs);
                }
            }
        }

        return false;
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
