package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.command.CompressionType;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read aggregate XML files from local, HDFS, and S3 locations using Spark's support " +
    "with each row being written to MarkLogic.")
public class ImportAggregateXmlCommand extends AbstractImportFilesCommand {

    @Parameter(required = true, names = "--element",
        description = "Specifies the local name of the element to use as the root of each document."
    )
    private String element;

    @Parameter(names = "--namespace",
        description = "Specifies the namespace of the element to use as the root of each document."
    )
    private String namespace;

    @Parameter(names = "--uriElement",
        description = "Specifies the local name of the element used for creating URIs."
    )
    private String uriElement;

    @Parameter(names = "--uriNamespace",
        description = "Specifies the namespace of the element used for creating URIs."
    )
    private String uriNamespace;

    @Parameter(names = "--compression",
        description = "When importing compressed files, specify the type of compression used."
    )
    private CompressionType compression;

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        return OptionsUtil.addOptions(
            super.makeReadOptions(),
            Options.READ_AGGREGATES_XML_ELEMENT, element,
            Options.READ_AGGREGATES_XML_NAMESPACE, namespace,
            Options.READ_AGGREGATES_XML_URI_ELEMENT, uriElement,
            Options.READ_AGGREGATES_XML_URI_NAMESPACE, uriNamespace,
            Options.READ_FILES_COMPRESSION, compression != null ? compression.name() : null
        );
    }
}
