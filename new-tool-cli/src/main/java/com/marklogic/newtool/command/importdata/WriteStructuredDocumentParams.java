package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * For import commands that can write "structured" rows with an arbitrary schema, either as JSON or XML documents.
 */
public class WriteStructuredDocumentParams extends WriteDocumentWithTemplateParams {

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @Parameter(
        names = "--xmlRootName",
        description = "Causes XML documents to be written instead of JSON, with the documents having a root element with this name."
    )
    private String xmlRootName;

    @Parameter(
        names = "--xmlNamespace",
        description = "Namespace for the root element of XML documents as specified by '--xmlRootName'."
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
}
