/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TdeInputs {

    protected String schemaName;
    protected String viewName;
    protected final Map<String, String> pathNamespaces = new HashMap<>();

    protected static final String DEFAULT_CONTEXT = "/";

    protected String uri;
    protected String context = DEFAULT_CONTEXT;
    protected String[] collections;
    protected String[] directories;
    protected String permissions;
    protected boolean disabled;
    protected String viewLayout;
    protected Map<String, String> columnVals;
    protected Map<String, String> columnTypes;
    protected Map<String, String> columnDefaultValues;
    protected Map<String, String> columnInvalidValues;
    protected Map<String, String> columnReindexing;
    protected Map<String, Set<String>> columnPermissions;
    protected Map<String, String> columnCollations;
    protected List<String> nullableColumns;

    public TdeInputs() {
    }

    public TdeInputs(String schemaName, String viewName) {
        this.schemaName = schemaName;
        this.viewName = viewName;
    }

    public TdeInputs withContext(String context) {
        if (context != null && !context.isEmpty()) {
            this.context = context;
        }
        return this;
    }

    public TdeInputs withUri(String uri) {
        if (uri != null && !uri.isEmpty()) {
            this.uri = uri;
        }
        return this;
    }

    public TdeInputs withJsonRootName(String jsonRootName) {
        if (jsonRootName != null && !jsonRootName.isEmpty()
            // Don't override the context if the user already specified one.
            && DEFAULT_CONTEXT.equals(this.context)) {
            this.context = "/" + jsonRootName;
        }
        return this;
    }

    public TdeInputs withXmlRootName(String xmlRootName, String namespace) {
        if (xmlRootName != null && !xmlRootName.isEmpty()
            // Don't override the context if the user already specified one.
            && DEFAULT_CONTEXT.equals(this.context)) {
            if (namespace != null) {
                this.pathNamespaces.put("ns1", namespace);
                this.context = "/ns1:" + xmlRootName;
            } else {
                this.context = "/" + xmlRootName;
            }
        }
        return this;
    }

    public TdeInputs withPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    public TdeInputs withCollections(String... collections) {
        this.collections = collections;
        return this;
    }

    public TdeInputs withDirectories(String... directories) {
        this.directories = directories;
        return this;
    }

    public TdeInputs withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public TdeInputs withViewLayout(String viewLayout) {
        this.viewLayout = viewLayout;
        return this;
    }

    public TdeInputs withColumnVals(Map<String, String> columnVals) {
        this.columnVals = columnVals;
        return this;
    }

    public TdeInputs withColumnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
        return this;
    }

    public TdeInputs withColumnDefaultValues(Map<String, String> columnDefaultValues) {
        this.columnDefaultValues = columnDefaultValues;
        return this;
    }

    public TdeInputs withColumnInvalidValues(Map<String, String> columnInvalidValues) {
        this.columnInvalidValues = columnInvalidValues;
        return this;
    }

    public TdeInputs withColumnReindexing(Map<String, String> columnReindexing) {
        this.columnReindexing = columnReindexing;
        return this;
    }

    public TdeInputs withColumnPermissions(Map<String, Set<String>> columnPermissions) {
        this.columnPermissions = columnPermissions;
        return this;
    }

    public TdeInputs withColumnCollations(Map<String, String> columnCollations) {
        this.columnCollations = columnCollations;
        return this;
    }

    public TdeInputs withNullableColumns(List<String> nullableColumns) {
        this.nullableColumns = nullableColumns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }

    public String getContext() {
        return context;
    }

    public String[] getCollections() {
        return collections;
    }

    public String[] getDirectories() {
        return directories;
    }

    public Map<String, String> getPathNamespaces() {
        return pathNamespaces;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getUri() {
        return uri;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getViewLayout() {
        return viewLayout;
    }

    public Map<String, String> getColumnVals() {
        return columnVals;
    }

    public Map<String, String> getColumnTypes() {
        return columnTypes;
    }

    public Map<String, String> getColumnDefaultValues() {
        return columnDefaultValues;
    }

    public Map<String, String> getColumnInvalidValues() {
        return columnInvalidValues;
    }

    public Map<String, String> getColumnReindexing() {
        return columnReindexing;
    }

    public Map<String, Set<String>> getColumnPermissions() {
        return columnPermissions;
    }

    public Map<String, String> getColumnCollations() {
        return columnCollations;
    }

    public List<String> getNullableColumns() {
        return nullableColumns;
    }
}
