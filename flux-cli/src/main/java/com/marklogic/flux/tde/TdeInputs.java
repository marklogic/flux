/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private String context = "/";
    private final Map<String, String> namespaces = new HashMap<>();
    private boolean json = true; // Default to JSON TDE, can be set to false for XML TDE

    // Temporary until this is configurable by the user.
    private List<String> collections = List.of("changeme");

    public TdeInputs(String schemaName, String viewName, Iterator<Column> columns) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.columns = columns;
    }

    public TdeInputs withJsonRootName(String jsonRootName) {
        if (jsonRootName != null) {
            this.context = "/" + jsonRootName;
        }
        return this;
    }

    public TdeInputs withXmlRootName(String xmlRootName, String namespace) {
        if (xmlRootName != null) {
            this.json = false;
            if (namespace != null) {
                this.namespaces.put("ns1", namespace);
                this.context = "/ns1:" + xmlRootName;
            } else {
                this.context = "/" + xmlRootName;
            }
        }
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

    public List<String> getCollections() {
        return collections;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public boolean isJson() {
        return json;
    }
}
