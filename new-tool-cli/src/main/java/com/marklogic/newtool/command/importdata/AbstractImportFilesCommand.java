package com.marklogic.newtool.command.importdata;

import com.marklogic.newtool.command.AbstractCommand;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class for commands that import files and write to MarkLogic.
 */
public abstract class AbstractImportFilesCommand extends AbstractCommand {

    /**
     * Subclass must define the format used for reading - e.g. "csv", "marklogic", etc.
     *
     * @return the name of the Spark data source or connector to pass to the Spark 'format(String)' method
     */
    protected abstract String getReadFormat();

    protected abstract ReadFilesParams getReadParams();

    protected abstract Supplier<Map<String, String>> getWriteParams();

    @Override
    protected final Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        ReadFilesParams readFilesParams = getReadParams();
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", readFilesParams.getPaths());
        }
        readFilesParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader
            .format(getReadFormat())
            .options(readFilesParams.makeOptions())
            .load(readFilesParams.getPaths().toArray(new String[]{}));
    }

    @Override
    protected final void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(getWriteParams().get())
            .mode(SaveMode.Append)
            .save();
    }
}
