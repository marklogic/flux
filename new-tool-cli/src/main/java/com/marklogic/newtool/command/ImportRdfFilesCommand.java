package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read RDF data from local, HDFS, and S3 files and write the data as managed triples documents in MarkLogic.")
public class ImportRdfFilesCommand extends AbstractImportFilesCommand {

    @Parameter(names = "--graph", description = "Specify the graph URI for each triple not already associated with a graph. If not set, " +
        "triples will be added to the default MarkLogic graph - http://marklogic.com/semantics#default-graph . ")
    private String graph;

    @Parameter(names = "--graphOverride", description = "Specify the graph URI for each triple to be included in, " +
        "even if is already associated with a graph.")
    private String graphOverride;

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putAll(OptionsUtil.makeOptions(
            Options.READ_FILES_TYPE, "rdf",
            Options.READ_FILES_COMPRESSION, compression != null ? compression.name() : null
        ));
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = super.makeWriteOptions();
        options.putAll(OptionsUtil.makeOptions(
            Options.WRITE_GRAPH, graph,
            Options.WRITE_GRAPH_OVERRIDE, graphOverride
        ));
        return options;
    }
}
