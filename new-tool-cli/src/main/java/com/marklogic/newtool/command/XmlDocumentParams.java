package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * For commands that read arbitrary rows and write documents to MarkLogic.
 */
public class XmlDocumentParams {

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

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_XML_ROOT_NAME, xmlRootName,
            Options.WRITE_XML_NAMESPACE, xmlNamespace
        );
    }
}
