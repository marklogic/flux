package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.FilesImporter;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * "Structured files" = rows with an arbitrary schema that can be converted into either JSON or XML documents.
 */
abstract class AbstractImportStructuredFilesCommand<T extends FilesImporter<T>> extends AbstractImportFilesCommand<T> {

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @ParametersDelegate
    private XmlDocumentParams xmlDocumentParams = new XmlDocumentParams();

    private Map<String, String> dynamicParams;
    private final String format;

    protected AbstractImportStructuredFilesCommand(String format) {
        this.format = format;
    }

    @Override
    protected final String getReadFormat() {
        return this.format;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        if (dynamicParams != null) {
            options.putAll(dynamicParams);
        }
        return options;
    }

    @Override
    protected final Map<String, String> makeWriteOptions() {
        Map<String, String> options = OptionsUtil.addOptions(super.makeWriteOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName
        );
        options.putAll(xmlDocumentParams.makeOptions());
        return options;
    }

    protected final void setDynamicParams(Map<String, String> dynamicParams) {
        this.dynamicParams = dynamicParams;
    }
}
