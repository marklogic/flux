package com.marklogic.newtool.command.custom;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.S3Params;
import com.marklogic.newtool.command.export.ReadRowsParams;
import com.marklogic.newtool.command.export.SaveModeConverter;
import org.apache.spark.sql.*;

import java.util.HashMap;
import java.util.Map;

public class CustomExportRowsCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadRowsParams readRowsParams = new ReadRowsParams();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--target", description = "Identifier for the Spark connector that is the target of data to export.")
    private String target;

    @DynamicParameter(
        names = "-P",
        description = "Specify any number of options to be passed to the connector identified by '--target'."
    )
    private Map<String, String> writerParams = new HashMap<>();

    @Parameter(names = "--mode", converter = SaveModeConverter.class,
        description = "Specifies how data is written if the path already exists. " +
            "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
    private SaveMode saveMode = SaveMode.Overwrite;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readRowsParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(target)
            .options(writerParams)
            .mode(saveMode)
            .save();
    }
}
