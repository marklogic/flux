package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

public class WriteStructuredDocumentParams extends WriteDocumentWithTemplateParams {

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @ParametersDelegate
    private XmlDocumentParams xmlDocumentParams = new XmlDocumentParams();

    @Override
    public Map<String, String> makeOptions() {
        Map<String, String> options = OptionsUtil.addOptions(
            super.makeOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName
        );
        options.putAll(xmlDocumentParams.makeOptions());
        return options;
    }
}
