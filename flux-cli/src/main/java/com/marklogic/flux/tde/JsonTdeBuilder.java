/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marklogicspark.marklogic.client.io.JacksonHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;

import java.util.Arrays;
import java.util.Iterator;

public class JsonTdeBuilder implements TdeBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public TdeTemplate buildTde(TdeInputs tdeInputs) {
        ObjectNode tde = MAPPER.createObjectNode();
        ObjectNode template = tde.putObject("template");
        template.put("context", tdeInputs.getContext());

        if (tdeInputs.isDisabled()) {
            template.put("enabled", false);
        }

        if (tdeInputs.getCollections() != null && tdeInputs.getCollections().length > 0) {
            ArrayNode collectionsArray = template.putArray("collections");
            for (String collection : tdeInputs.getCollections()) {
                collectionsArray.add(collection);
            }
        }

        if (tdeInputs.getDirectories() != null && tdeInputs.getDirectories().length > 0) {
            ArrayNode directoriesArray = template.putArray("directories");
            for (String directory : tdeInputs.getDirectories()) {
                directoriesArray.add(directory);
            }
        }

        ObjectNode row = template.putArray("rows").addObject();
        row.put("schemaName", tdeInputs.getSchemaName());
        row.put("viewName", tdeInputs.getViewName());

        final String viewLayout = tdeInputs.getViewLayout();
        if (viewLayout != null && !viewLayout.isEmpty()) {
            row.put("viewLayout", viewLayout);
        }

        addColumns(row, tdeInputs);
        return new JsonTemplate(tde, tdeInputs.getPermissions(), tdeInputs);
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
            addColumn(columnsArray, column, tdeInputs);
        }
    }

    private static void addColumn(ArrayNode columns, TdeInputs.Column column, TdeInputs inputs) {
        ObjectNode columnNode = buildColumnWithRequiredFields(columns, column, inputs);
        final String name = column.getName();

        if (inputs.getNullableColumns() != null && inputs.getNullableColumns().contains(name)) {
            columnNode.put("nullable", true);
        }

        if (inputs.getColumnDefaultValues() != null && inputs.getColumnDefaultValues().containsKey(name)) {
            columnNode.put("default", inputs.getColumnDefaultValues().get(name));
        }

        if (inputs.getColumnInvalidValues() != null && inputs.getColumnInvalidValues().containsKey(name)) {
            columnNode.put("invalidValues", inputs.getColumnInvalidValues().get(name));
        }

        if (inputs.getColumnReindexing() != null && inputs.getColumnReindexing().containsKey(name)) {
            columnNode.put("reindexing", inputs.getColumnReindexing().get(name));
        }

        if (inputs.getColumnPermissions() != null && inputs.getColumnPermissions().containsKey(name)) {
            ArrayNode array = columnNode.putArray("permissions");
            Arrays.stream(inputs.getColumnPermissions().get(name).split(","))
                .map(String::trim)
                .forEach(array::add);
        }

        if (inputs.getColumnCollations() != null && inputs.getColumnCollations().containsKey(name)) {
            columnNode.put("collation", inputs.getColumnCollations().get(name));
        }
    }

    private static ObjectNode buildColumnWithRequiredFields(ArrayNode columns, TdeInputs.Column column, TdeInputs inputs) {
        ObjectNode columnNode = columns.addObject();
        final String name = column.getName();
        columnNode.put("name", name);

        final String scalarType = (inputs.getColumnTypes() != null && inputs.getColumnTypes().containsKey(name))
            ? inputs.getColumnTypes().get(name) : column.getScalarType();
        columnNode.put("scalarType", scalarType);

        final String val = (inputs.getColumnVals() != null && inputs.getColumnVals().containsKey(name))
            ? inputs.getColumnVals().get(name) : column.getVal();
        columnNode.put("val", val);

        return columnNode;
    }

    private static class JsonTemplate implements TdeTemplate {
        private final ObjectNode tdeTemplate;
        private final String permissions;
        private final String uri;

        public JsonTemplate(ObjectNode tdeTemplate, String permissions, TdeInputs inputs) {
            this.tdeTemplate = tdeTemplate;
            this.permissions = permissions;
            final String inputUri = inputs.getUri();
            this.uri = inputUri != null && !inputUri.isEmpty() ? inputUri :
                String.format("/tde/%s/%s.json", inputs.getSchemaName(), inputs.getViewName());
        }

        @Override
        public AbstractWriteHandle getWriteHandle() {
            return new JacksonHandle(tdeTemplate);
        }

        @Override
        public String toPrettyString() {
            return tdeTemplate.toPrettyString();
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public String getPermissions() {
            return permissions;
        }
    }
}
