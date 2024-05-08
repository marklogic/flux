package com.marklogic.newtool.api;

/**
 * Defines options for writing "structured" documents - i.e. JSON or XML documents created from all the columns in
 * the rows returned by a reader (as opposed to "document" rows where the document is defined by the "content" column).
 */
public interface WriteStructuredDocumentsOptions extends WriteDocumentsOptions<WriteStructuredDocumentsOptions> {

    WriteStructuredDocumentsOptions jsonRootName(String jsonRootName);

    WriteStructuredDocumentsOptions xmlRootName(String xmlRootName);

    WriteStructuredDocumentsOptions xmlNamespace(String xmlNamespace);
}
