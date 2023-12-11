package com.marklogic.newtool.command;

import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Base class for anything that reads from MarkLogic and exports it elsewhere.
 */
public abstract class AbstractExportCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadParams readParams = new ReadParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(readParams.makeOptions())
            .load();
    }

    public ReadParams getReadParams() {
        return readParams;
    }

    public void setReadParams(ReadParams readParams) {
        this.readParams = readParams;
    }
}
