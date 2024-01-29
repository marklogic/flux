package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "to MarkLogic.")
public class ImportAvroFilesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true."
    )
    private Map<String, String> avroParams = new HashMap<>();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", paths);
        }
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format("avro").options(makeReadOptions())
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
        options.putAll(avroParams);
        return options;
    }

    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.putAll(writeDocumentParams.makeOptions());
        return options;
    }
}
