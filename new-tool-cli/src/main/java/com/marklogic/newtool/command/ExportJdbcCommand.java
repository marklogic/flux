package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

@Parameters(commandDescription = "Read rows via Optic and write via JDBC")
public class ExportJdbcCommand extends AbstractExportCommand {

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--mode")
    private SaveMode mode = SaveMode.Overwrite;

    @Override
    protected void writeDataset(SparkSession session, DataFrameWriter<Row> writer) {
        writer.mode(getMode())
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());
    }

    public JdbcParams getJdbcParams() {
        return jdbcParams;
    }

    public void setJdbcParams(JdbcParams jdbcParams) {
        this.jdbcParams = jdbcParams;
    }

    public SaveMode getMode() {
        return mode;
    }

    public void setMode(SaveMode mode) {
        this.mode = mode;
    }
}
