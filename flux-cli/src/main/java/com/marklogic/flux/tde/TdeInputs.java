/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TdeInputs {

    private final String schemaName;
    private final String viewName;

    private static final String DEFAULT_CONTEXT = "/";

    private String uri;
    private String context = DEFAULT_CONTEXT;
    private String contextNamespaceUri;
    private String contextNamespacePrefix;

    private String[] collections;
    private String[] directories;
    private String permissions;
    private boolean disabled;
    private boolean viewVirtual;
    private String viewLayout;
    private Map<String, String> columnVals;
    private Map<String, String> columnTypes;
    private Map<String, String> columnDefaultValues;
    private Map<String, String> columnInvalidValues;
    private Map<String, String> columnReindexing;
    private Map<String, Set<String>> columnPermissions;
    private Map<String, String> columnCollations;
    private List<String> nullableColumns;
    private List<String> virtualColumns;
    private Map<String, Integer> columnDimensions;
    private Map<String, Float> columnAnnCompressions;
    private Map<String, String> columnAnnDistances;
    private Map<String, Boolean> columnAnnIndexed;

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

    public TdeInputs withXmlRootName(String xmlRootName, String namespaceUri) {
        return withXmlRootName(xmlRootName, namespaceUri, "ns1");
    }

    public TdeInputs withXmlRootName(String xmlRootName, String namespaceUri, String namespacePrefix) {
        if (xmlRootName != null && !xmlRootName.isEmpty()
            // Don't override the context if the user already specified one.
            && DEFAULT_CONTEXT.equals(this.context)) {
            if (namespaceUri != null) {
                this.contextNamespaceUri = namespaceUri;
                this.contextNamespacePrefix = namespacePrefix;
                this.context = String.format("/%s:%s", namespacePrefix, xmlRootName);
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

    public TdeInputs withViewVirtual(boolean viewVirtual) {
        this.viewVirtual = viewVirtual;
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

    public TdeInputs withVirtualColumns(List<String> virtualColumns) {
        this.virtualColumns = virtualColumns;
        return this;
    }

    public TdeInputs withColumnDimensions(Map<String, Integer> columnDimensions) {
        this.columnDimensions = columnDimensions;
        return this;
    }

    public TdeInputs withColumnAnnCompressions(Map<String, Float> columnAnnCompressions) {
        this.columnAnnCompressions = columnAnnCompressions;
        return this;
    }

    public TdeInputs withColumnAnnDistances(Map<String, String> columnAnnDistances) {
        this.columnAnnDistances = columnAnnDistances;
        return this;
    }

    public TdeInputs withColumnAnnIndexed(Map<String, Boolean> columnAnnIndexed) {
        this.columnAnnIndexed = columnAnnIndexed;
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

    public String getPermissions() {
        return permissions;
    }

    public String getUri() {
        return uri;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isViewVirtual() {
        return viewVirtual;
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

    public List<String> getVirtualColumns() {
        return virtualColumns;
    }

    public Map<String, Integer> getColumnDimensions() {
        return columnDimensions;
    }

    public Map<String, Float> getColumnAnnCompressions() {
        return columnAnnCompressions;
    }

    public Map<String, String> getColumnAnnDistances() {
        return columnAnnDistances;
    }

    public Map<String, Boolean> getColumnAnnIndexed() {
        return columnAnnIndexed;
    }

    public boolean hasContextNamespaceWithPrefix() {
        return contextNamespaceUri != null && contextNamespacePrefix != null
            && !contextNamespaceUri.trim().isEmpty() && !contextNamespacePrefix.trim().isEmpty();
    }

    public String getContextNamespaceUri() {
        return contextNamespaceUri;
    }

    public String getContextNamespacePrefix() {
        return contextNamespacePrefix;
    }
}
