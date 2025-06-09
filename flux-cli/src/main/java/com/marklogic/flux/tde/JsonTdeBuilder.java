/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marklogicspark.marklogic.client.io.JacksonHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;

import java.util.Iterator;
import java.util.Set;

public class JsonTdeBuilder implements TdeBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public TdeTemplate buildTde(TdeInputs tdeInputs, Iterator<Column> columns) {
        ObjectNode tde = MAPPER.createObjectNode();
        ObjectNode template = tde.putObject("template");
        template.put("context", tdeInputs.getContext());

        if (tdeInputs.getPathNamespaces() != null) {
            ArrayNode array = template.putArray("pathNamespace");
            tdeInputs.getPathNamespaces().entrySet().forEach(entry -> {
                ObjectNode namespace = array.addObject();
                namespace.put("prefix", entry.getKey());
                namespace.put("namespaceUri", entry.getValue());
            });
        }

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

        addColumns(row, columns);
        return new JsonTemplate(tde, tdeInputs.getPermissions(), tdeInputs);
    }

    private static void addColumns(ObjectNode row, Iterator<Column> columns) {
        ArrayNode columnsArray = row.putArray("columns");
        while (columns.hasNext()) {
            final Column column = columns.next();
            final String scalarType = column.getScalarType();
            if (scalarType == null) {
                continue;
            }
            addColumn(columnsArray, column);
        }
    }

    private static void addColumn(ArrayNode columns, Column column) {
        ObjectNode columnNode = columns.addObject();
        columnNode.put("name", column.getName());
        columnNode.put("scalarType", column.getScalarType());
        columnNode.put("val", column.getVal());

        if (column.isNullable()) {
            columnNode.put("nullable", true);
        }

        String defaultValue = column.getDefaultValue();
        if (defaultValue != null) {
            columnNode.put("default", defaultValue);
        }

        String invalidValues = column.getInvalidValues();
        if (invalidValues != null) {
            columnNode.put("invalidValues", invalidValues);
        }

        String reindexing = column.getReindexing();
        if (reindexing != null) {
            columnNode.put("reindexing", reindexing);
        }

        Set<String> permissions = column.getPermissions();
        if (permissions != null) {
            ArrayNode array = columnNode.putArray("permissions");
            permissions.stream()
                .map(String::trim)
                .forEach(array::add);
        }

        String collation = column.getCollation();
        if (collation != null) {
            columnNode.put("collation", collation);
        }
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
