package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.CompressionType;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

@Parameters(commandDescription = "Read RDF data from local, HDFS, and S3 files and write the data as managed triples documents in MarkLogic.")
public class ImportRdfFilesCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadFilesParams readFilesParams = new ReadFilesParams();

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @Parameter(names = "--graph", description = "Specify the graph URI for each triple not already associated with a graph. If not set, " +
        "triples will be added to the default MarkLogic graph - http://marklogic.com/semantics#default-graph . ")
    private String graph;

    @Parameter(names = "--graphOverride", description = "Specify the graph URI for each triple to be included in, " +
        "even if is already associated with a graph.")
    private String graphOverride;

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", readFilesParams.getPaths());
        }
        readFilesParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader
            .format(MARKLOGIC_CONNECTOR)
            .options(readFilesParams.makeOptions())
            .options(OptionsUtil.makeOptions(
                Options.READ_FILES_TYPE, "rdf",
                Options.READ_FILES_COMPRESSION, compression != null ? compression.name() : null
            ))
            .load(readFilesParams.getPaths().toArray(new String[]{}));
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeDocumentParams.makeOptions())
            .options(OptionsUtil.makeOptions(
                Options.WRITE_GRAPH, graph,
                Options.WRITE_GRAPH_OVERRIDE, graphOverride
            ))
            .mode(SaveMode.Append)
            .save();
    }
}
