package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read aggregate XML files from local, HDFS, and S3 locations using Spark's support " +
    "with each row being written to MarkLogic.")
public class ImportAggregateXmlCommand extends AbstractImportFilesCommand {

    @Parameter(required = true, names = "--element",
        description = "Specifies the local name of the element to use as the root of each document.")
    private String element;

    @Parameter(required = false, names = "--namespace", description = "Specifies namespace where the input data resides.")
    private String namespace;

    @Parameter(required = false, names = "--uriElement",
        description = "Specifies the local name of the element used for creating uris of docs inserted into MarkLogic.")
    private String uriElement;

    @Parameter(required = false, names = "--uriNamespace",
        description = "Specifies namespace where the input data resides.")
    private String uriNamespace;

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.put(Options.READ_AGGREGATES_XML_ELEMENT, element);
        options.put(Options.READ_AGGREGATES_XML_NAMESPACE, namespace);
        options.put(Options.READ_AGGREGATES_XML_URI_ELEMENT, uriElement);
        options.put(Options.READ_AGGREGATES_XML_URI_NAMESPACE, uriNamespace);
        if (compression != null) {
            options.put(Options.READ_FILES_COMPRESSION, compression.name());
        }
        return options;
    }
}
