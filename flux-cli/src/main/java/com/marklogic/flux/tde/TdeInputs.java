/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TdeInputs {

    interface Column {
        String getName();

        default String getVal() {
            return getName();
        }

        String getScalarType();
    }

    private final String schemaName;
    private final String viewName;
    private final Iterator<Column> columns;
    private final Map<String, String> namespaces = new HashMap<>();

    private static final String DEFAULT_CONTEXT = "/";

    private String uri;
    private String context = DEFAULT_CONTEXT;
    private String[] collections;
    private String[] directories;
    private String permissions;
    private boolean json = true; // Default to JSON TDE, can be set to false for XML TDE
    private boolean disabled;
    private String viewLayout;

    public TdeInputs(String schemaName, String viewName, Iterator<Column> columns) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.columns = columns;
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
        if (jsonRootName != null && !jsonRootName.isEmpty()) {
            this.json = true;
            // Don't override the context if the user already specified one.
            if (DEFAULT_CONTEXT.equals(this.context)) {
                this.context = "/" + jsonRootName;
            }
        }
        return this;
    }

    public TdeInputs withXmlRootName(String xmlRootName, String namespace) {
        if (xmlRootName != null && !xmlRootName.isEmpty()) {
            this.json = false;
            // Don't override the context if the user already specified one.
            if (DEFAULT_CONTEXT.equals(this.context)) {
                if (namespace != null) {
                    this.namespaces.put("ns1", namespace);
                    this.context = "/ns1:" + xmlRootName;
                } else {
                    this.context = "/" + xmlRootName;
                }
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

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }

    public Iterator<Column> getColumns() {
        return columns;
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

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public boolean isJson() {
        return json;
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
}
