package com.marklogic.newtool.impl.export;

import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.Executor;
import com.marklogic.newtool.api.WriteFilesOptions;
import com.marklogic.newtool.impl.AbstractCommand;
import com.marklogic.newtool.impl.SparkUtil;
import org.apache.spark.sql.*;

/**
 * Support class for concrete commands that want to run an Optic DSL query to read rows and then write them to one or
 * more files.
 */
abstract class AbstractExportRowsToFilesCommand<T extends Executor> extends AbstractCommand<T> {

    @ParametersDelegate
    protected final ReadRowsParams readParams = new ReadRowsParams();

    // Sonar complaining about the use of ?; not sure yet how to "fix" it, so ignoring.
    @SuppressWarnings("java:S1452")
    protected abstract WriteStructuredFilesParams<? extends WriteFilesOptions> getWriteFilesParams();

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
        WriteStructuredFilesParams<? extends WriteFilesOptions> writeParams = getWriteFilesParams();
        writeParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(getWriteFormat())
            .options(writeParams.get())
            .mode(SparkUtil.toSparkSaveMode(writeParams.getSaveMode()))
            .save(writeParams.getPath());
    }
}
