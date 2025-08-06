/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Defines options for writing "structured" documents - i.e. JSON or XML documents created from all the columns in
 * the rows returned by a reader (as opposed to "document" rows where the document is defined by the "content" column).
 */
public interface WriteStructuredDocumentsOptions extends WriteDocumentsOptions<WriteStructuredDocumentsOptions> {

    WriteStructuredDocumentsOptions jsonRootName(String jsonRootName);

    WriteStructuredDocumentsOptions xmlRootName(String xmlRootName);

    WriteStructuredDocumentsOptions xmlNamespace(String xmlNamespace);

    /**
     * @param value Ignore fields with null values in the data source when writing JSON or XML documents to MarkLogic.
     *              Fields with null values are included by default.
     * @return an instance of these options.
     */
    WriteStructuredDocumentsOptions ignoreNullFields(boolean value);
}
