package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Parameters(commandDescription = "Read aggregate XML files from local, HDFS, and S3 locations using Spark's support " +
    "with each row being written to MarkLogic.")
public class ImportAggregateXmlCommand extends AbstractCommand {
    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

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
    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", paths);
        }
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(makeReadOptions())
            .load(paths.toArray(new String[]{}));
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.put(Options.READ_AGGREGATES_XML_ELEMENT, element);
        options.put(Options.READ_AGGREGATES_XML_NAMESPACE, namespace);
        options.put(Options.READ_AGGREGATES_XML_URI_ELEMENT, uriElement);
        options.put(Options.READ_AGGREGATES_XML_URI_NAMESPACE, uriNamespace);
        return options;
    }

    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.putAll(writeDocumentParams.makeOptions());
        return options;
    }
}
