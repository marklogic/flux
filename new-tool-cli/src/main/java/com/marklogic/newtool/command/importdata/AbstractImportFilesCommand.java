package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import org.apache.spark.sql.*;

import java.util.Map;

/**
 * Base class for commands that import files and write to MarkLogic.
 */
public abstract class AbstractImportFilesCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadFilesParams readFilesParams = new ReadFilesParams();

    @ParametersDelegate
    private WriteDocumentWithTemplateParams writeDocumentParams = new WriteDocumentWithTemplateParams();

    /**
     * Subclass must define the format used for reading - e.g. "csv", "marklogic", etc.
     *
     * @return
     */
    protected abstract String getReadFormat();

    @Override
    protected final Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", readFilesParams.getPaths());
        }
        readFilesParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader
            .format(getReadFormat())
            .options(makeReadOptions())
            .load(readFilesParams.getPaths().toArray(new String[]{}));
    }

    @Override
    protected final void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }

    /**
     * Subclasses can override this to add their own options.
     *
     * @return
     */
    protected Map<String, String> makeReadOptions() {
        return readFilesParams.makeOptions();
    }

    /**
     * Subclasses can override this to add their own options.
     *
     * @return
     */
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.putAll(writeDocumentParams.makeOptions());
        return options;
    }
}
