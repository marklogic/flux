package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

import java.util.Map;

public abstract class AbstractExportFilesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @Parameter(names = "--mode", converter = SaveModeConverter.class,
        description = "Specifies how data is written if the path already exists. " +
            "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
    private SaveMode saveMode = SaveMode.Overwrite;

    @ParametersDelegate
    private ReadRowsParams readRowsParams = new ReadRowsParams();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    protected abstract String getWriteFormat();

    protected abstract Map<String, String> makeWriteOptions();

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
        writer.format(getWriteFormat())
            .options(makeWriteOptions())
            .mode(saveMode)
            .save(path);
    }
}
