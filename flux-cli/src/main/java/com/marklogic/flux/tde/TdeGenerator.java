/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * Trying to keep this class decoupled from Spark for now - the thought being that it could move into
 * the Java Client API once it matures a bit more.
 */
public abstract class TdeGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectNode generateTde(TdeInputs tdeInputs) {
        ObjectNode tde = MAPPER.createObjectNode();
        ObjectNode template = tde.putObject("template");
        template.put("context", tdeInputs.getContext());

        if (tdeInputs.getCollections() != null) {
            ArrayNode collectionsArray = template.putArray("collections");
            for (String collection : tdeInputs.getCollections()) {
                collectionsArray.add(collection);
            }
        }

        ObjectNode row = template.putArray("rows").addObject();
        row.put("schemaName", tdeInputs.getSchemaName());
        row.put("viewName", tdeInputs.getViewName());
        addColumns(row, tdeInputs);

        return tde;
    }

    private static void addColumns(ObjectNode row, TdeInputs tdeInputs) {
        ArrayNode columnsArray = row.putArray("columns");
        Iterator<TdeInputs.Column> columns = tdeInputs.getColumns();
        while (columns.hasNext()) {
            final TdeInputs.Column column = columns.next();
            final String scalarType = column.getScalarType();
            if (scalarType == null) {
                continue;
            }
            ObjectNode columnNode = columnsArray.addObject();
            columnNode.put("name", column.getName());
            columnNode.put("val", column.getVal());
            columnNode.put("scalarType", scalarType);
        }
    }

    private TdeGenerator() {
        //  No reason for this class to have any state yet.
    }
}
