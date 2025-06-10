/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.TdeOptions;
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
import java.util.function.Consumer;

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

    @CommandLine.Mixin
    private final TdeParams tdeParams = new TdeParams();

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
     */
    public boolean generateTde(StructType sparkSchema, ConnectionParams connectionParams) {
        if (tdeParams.getTdeSchema() != null && tdeParams.getTdeView() != null) {
            TdeInputs inputs = tdeParams.buildTdeInputs(jsonRootName, xmlRootName, xmlNamespace);
            TdeBuilder tdeBuilder = "xml".equalsIgnoreCase(tdeParams.getTdeDocumentType()) ? new XmlTdeBuilder() : new JsonTdeBuilder();
            TdeTemplate tdeTemplate = tdeBuilder.buildTde(inputs, new SparkColumnIterator(sparkSchema, inputs));
            if (tdeParams.isTdePreview()) {
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

    protected TdeInputs buildTdeInputs() {
        return tdeParams.buildTdeInputs(jsonRootName, xmlRootName, xmlNamespace);
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

    @Override
    public WriteStructuredDocumentsOptions tdeOptions(Consumer<TdeOptions> tdeOptions) {
        tdeOptions.accept(tdeParams);
        return this;
    }
}
