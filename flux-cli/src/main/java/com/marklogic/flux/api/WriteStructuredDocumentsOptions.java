/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

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

    /**
     * @param tdeOptions Consumer function for configuring TDE template generation options.
     * @return an instance of these options.
     * @since 1.4.0
     */
    WriteStructuredDocumentsOptions tdeOptions(Consumer<TdeOptions> tdeOptions);
}
