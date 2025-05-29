/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.Iterator;
import java.util.List;

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

    // Temporary until this is configurable by the user.
    private List<String> collections = List.of("changeme");

    public TdeInputs(String schemaName, String viewName, Iterator<Column> columns) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.columns = columns;
    }

    public TdeInputs withJsonRootName(String jsonRootName) {
        this.context = jsonRootName != null ? jsonRootName : "/";
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
}
