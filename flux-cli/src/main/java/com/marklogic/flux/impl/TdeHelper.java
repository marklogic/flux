/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.impl.importdata.SparkColumnIterator;
import com.marklogic.flux.impl.importdata.TdeParams;
import com.marklogic.flux.tde.*;
import com.marklogic.spark.ContextSupport;
import com.marklogic.spark.Util;
import marklogicspark.marklogic.client.DatabaseClient;
import org.apache.spark.sql.types.StructType;

/**
 * Acts as a bridge between Flux and the intended-to-be-generic TDE template generation functionality.
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

    public enum Result {
        TEMPLATE_LOGGED,
        TEMPLATE_LOADED
    }

    public TdeInputs buildTdeInputs() {
        return tdeParams.buildTdeInputs(jsonRootName, xmlRootName, xmlNamespace);
    }

    public Result logOrLoadTemplate(StructType sparkSchema, ConnectionParams connectionParams) {
        if (tdeParams.hasSchemaAndView()) {
            TdeInputs tdeInputs = buildTdeInputs();
            TdeBuilder tdeBuilder = "xml".equalsIgnoreCase(tdeParams.getDocumentType()) ? new XmlTdeBuilder() : new JsonTdeBuilder();
            TdeTemplate tdeTemplate = tdeBuilder.buildTde(tdeInputs, new SparkColumnIterator(sparkSchema, tdeInputs));

            if (tdeParams.isPreview()) {
                if (Util.MAIN_LOGGER.isInfoEnabled()) {
                    Util.MAIN_LOGGER.info("Generated TDE:\n{}", tdeTemplate.toPrettyString());
                }
                return Result.TEMPLATE_LOGGED;
            } else {
                try (DatabaseClient client = new ContextSupport(connectionParams.makeOptions()).connectToMarkLogic()) {
                    new TdeLoader(client).loadTde(tdeTemplate);
                    return Result.TEMPLATE_LOADED;
                }
            }
        }

        return null;
    }
}
