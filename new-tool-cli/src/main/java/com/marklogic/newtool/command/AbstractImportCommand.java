package com.marklogic.newtool.command;

import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

/**
 * Base class for import job where "import" = read from somewhere, write to MarkLogic.
 */
public abstract class AbstractImportCommand extends AbstractCommand {

    @ParametersDelegate
    private WriteParams writeParams = new WriteParams();

    @Override
    protected void writeDataset(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    public WriteParams getWriteParams() {
        return writeParams;
    }

    public void setWriteParams(WriteParams writeParams) {
        this.writeParams = writeParams;
    }
}
