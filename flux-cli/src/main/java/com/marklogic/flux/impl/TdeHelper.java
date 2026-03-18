/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.impl.importdata.SparkColumnIterator;
import com.marklogic.flux.impl.importdata.TdeParams;
import com.marklogic.flux.tde.*;
import com.marklogic.spark.ContextSupport;
import com.marklogic.spark.Util;
import com.marklogic.client.DatabaseClient;
import org.apache.spark.sql.types.StructType;
import java.util.Iterator;

/**
 * Acts as a bridge between Flux and the intended-to-be-generic TDE template generation functionality.
 */
public class TdeHelper {

    private final TdeParams tdeParams;
    private final String jsonRootName;
    private final String xmlRootName;
    private final String xmlNamespace;
    private final String incrementalWriteHashName;
    private final boolean includeIncrementalWriteColumns;

    public TdeHelper(TdeParams tdeParams, String jsonRootName, String xmlRootName, String xmlNamespace, String incrementalWriteHashName) {
        this.tdeParams = tdeParams;
        this.jsonRootName = jsonRootName;
        this.xmlRootName = xmlRootName;
        this.xmlNamespace = xmlNamespace;
        this.incrementalWriteHashName = incrementalWriteHashName;
        this.includeIncrementalWriteColumns = tdeParams.isIncludeIncrementalWriteColumns();
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
            Iterator<TdeBuilder.Column> columns = new SparkColumnIterator(sparkSchema, tdeInputs);
            if (includeIncrementalWriteColumns) {
                columns = new IncrementalWriteColumnIterator(columns, incrementalWriteHashName);
            }
            TdeTemplate tdeTemplate = tdeBuilder.buildTde(tdeInputs, columns);

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
