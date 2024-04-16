package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * For commands where defining URIs via a template makes sense.
 */
public class WriteDocumentWithTemplateParams extends WriteDocumentParams {

    @Parameter(
        names = "--uriTemplate",
        description = "String defining a template for constructing each document URI. " +
            "See https://marklogic.github.io/marklogic-spark-connector/writing.html for more information."
    )
    private String uriTemplate;

    @Override
    public Map<String, String> makeOptions() {
        return OptionsUtil.addOptions(
            super.makeOptions(),
            Options.WRITE_URI_TEMPLATE, uriTemplate
        );
    }
}
