package com.marklogic.newtool.command;

import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

/**
 * This effectively boils down to "Read from MarkLogic, write/process with MarkLogic".
 * <p>
 * Note that we don't need a way to specify different connection params for the writer because a user can already do
 * that via the "-W:" dynamic param.
 */
public class ProcessContentCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadParams readParams = new ReadParams();

    @ParametersDelegate
    private WriteParams writeParams = new WriteParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void writeDataset(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    public ReadParams getReadParams() {
        return readParams;
    }

    public void setReadParams(ReadParams readParams) {
        this.readParams = readParams;
    }

    public WriteParams getWriteParams() {
        return writeParams;
    }

    public void setWriteParams(WriteParams writeParams) {
        this.writeParams = writeParams;
    }
}
