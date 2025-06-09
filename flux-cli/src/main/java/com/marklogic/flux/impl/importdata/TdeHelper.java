/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.ConnectionParams;
import com.marklogic.flux.tde.*;
import com.marklogic.spark.ContextSupport;
import com.marklogic.spark.Util;
import marklogicspark.marklogic.client.DatabaseClient;
import org.apache.spark.sql.types.StructType;

/**
 * Acts as the bridge between the command classes that collect all the TDE-specific inputs and the classes for
 * building and loading a TDE based on all the inputs. Supports both use cases of previewing a TDE by logging it and
 * actually loading a TDE into MarkLogic.
 */
public class TdeHelper {

    private final TdeParams tdeParams;
    private final String jsonRootName;
    private final String xmlRootName;
    private final String xmlNamespace;

    public TdeHelper(TdeParams tdeParams, String jsonRootName, String xmlRootName, String xmlNamespace) {
        this.tdeParams = tdeParams;
        this.jsonRootName = jsonRootName;
        this.xmlRootName = xmlRootName;
        this.xmlNamespace = xmlNamespace;
    }

    public boolean logTemplateIfNecessary(StructType sparkSchema) {
        if (tdeParams.getSchemaName() != null && tdeParams.getViewName() != null && tdeParams.isPreview() && Util.MAIN_LOGGER.isInfoEnabled()) {
            TdeTemplate tdeTemplate = buildTdeTemplate(sparkSchema);
            Util.MAIN_LOGGER.info("Generated TDE:\n{}", tdeTemplate.toPrettyString());
            return true;
        }
        return false;
    }

    public void loadTemplateIfNecessary(StructType sparkSchema, ConnectionParams connectionParams) {
        if (tdeParams.getSchemaName() != null && tdeParams.getViewName() != null && !tdeParams.isPreview()) {
            TdeTemplate tdeTemplate = buildTdeTemplate(sparkSchema);
            try (DatabaseClient client = new ContextSupport(connectionParams.makeOptions()).connectToMarkLogic()) {
                new TdeLoader(client).loadTde(tdeTemplate);
            }
        }
    }

    private TdeTemplate buildTdeTemplate(StructType sparkSchema) {
        // This is awkward. We want a TdeParams object that captures all the TDE-specific fields - but the JSON/XML
        // root fields are not specific to the TDE yet must affect the TDE context if the user didn't already
        // specify a TDE context. Probably needs another refactor so that this modification doesn't occur - i.e. some
        // sort of "default context provider" that gets passed to the TdeBuilder.
        tdeParams.withJsonRootName(jsonRootName);
        tdeParams.withXmlRootName(xmlRootName, xmlNamespace);
        TdeBuilder tdeBuilder = "xml".equalsIgnoreCase(tdeParams.getDocumentType()) ? new XmlTdeBuilder() : new JsonTdeBuilder();
        return tdeBuilder.buildTde(tdeParams, new SparkColumnIterator(sparkSchema, tdeParams));
    }
}
