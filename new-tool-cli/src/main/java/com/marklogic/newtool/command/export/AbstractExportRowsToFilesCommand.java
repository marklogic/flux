package com.marklogic.newtool.command.export;

import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import org.apache.spark.sql.*;

/**
 * Support class for concrete commands that want to run an Optic DSL query to read rows and then write them to one or
 * more files.
 */
abstract class AbstractExportRowsToFilesCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadRowsParams readParams = new ReadRowsParams();

    protected abstract WriteStructuredFilesParams getWriteFilesParams();

    /**
     * @return subclass must return the Spark data format for the desired output file.
     */
    protected abstract String getWriteFormat();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        final Integer fileCount = getWriteFilesParams().getFileCount();
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        WriteStructuredFilesParams writeParams = getWriteFilesParams();
        writeParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(getWriteFormat())
            .options(writeParams.get())
            .mode(writeParams.getSaveMode())
            .save(writeParams.getPath());
    }
}
