package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

/**
 * Idea is that "export" = read from MarkLogic using Optic and then do something with the results.
 */
public abstract class AbstractExportCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadParams readParams = new ReadParams();

    @Parameter(names = "--mode")
    private SaveMode mode = SaveMode.Append;

    protected final Dataset<Row> read(SparkSession session) {
        return session.read()
            .format(MARKLOGIC_CONNECTOR)
            .options(makeReadOptions())
            .options(readParams.makeOptions())
            .load();
    }

    public ReadParams getReadParams() {
        return readParams;
    }

    public void setReadParams(ReadParams readParams) {
        this.readParams = readParams;
    }

    public SaveMode getMode() {
        return mode;
    }

    public void setMode(SaveMode mode) {
        this.mode = mode;
    }
}
