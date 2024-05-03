package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.S3Params;
import org.apache.spark.sql.*;

import java.util.Map;

/**
 * Support class for concrete commands that want to run an Optic DSL query to read rows and then write them to one or
 * more files.
 */
abstract class AbstractExportRowsToFilesCommand extends AbstractCommand {

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

    @Parameter(names = "--fileCount", description = "Specifies how many files should be written; also an alias for '--repartition'.")
    private Integer fileCount = 1;

    /**
     * @return subclass must return the Spark data format for the desired output file.
     */
    protected abstract String getWriteFormat();

    /**
     * Allows subclass a chance to provide their own output-specific write options.
     *
     * @return a map of options to pass to the Spark writer
     */
    protected abstract Map<String, String> makeWriteOptions();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
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
