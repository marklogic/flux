/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;

/**
 * For import commands that can write "structured" rows with an arbitrary schema, either as JSON or XML documents.
 */
public class WriteStructuredDocumentParams extends WriteDocumentParams<WriteStructuredDocumentsOptions> implements WriteStructuredDocumentsOptions {

    @CommandLine.Option(
        names = "--json-root-name",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @CommandLine.Option(
        names = "--xml-root-name",
        description = "Causes XML documents to be written instead of JSON, with each document having a root element with this name."
    )
    private String xmlRootName;

    @CommandLine.Option(
        names = "--xml-namespace",
        description = "Namespace for the root element of XML documents as specified by '--xml-root-name'."
    )
    private String xmlNamespace;

    @Override
    public Map<String, String> makeOptions() {
        return OptionsUtil.addOptions(
            super.makeOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName,
            Options.WRITE_XML_ROOT_NAME, xmlRootName,
            Options.WRITE_XML_NAMESPACE, xmlNamespace
        );
    }

    @Override
    public WriteStructuredDocumentsOptions jsonRootName(String jsonRootName) {
        this.jsonRootName = jsonRootName;
        return this;
    }

    @Override
    public WriteStructuredDocumentsOptions xmlRootName(String xmlRootName) {
        this.xmlRootName = xmlRootName;
        return this;
    }

    @Override
    public WriteStructuredDocumentsOptions xmlNamespace(String xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
        return this;
    }
}
