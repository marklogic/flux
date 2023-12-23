package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

@Parameters(commandDescription = "Read rows via Optic and write via JDBC")
public class ExportJdbcCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadParams readParams = new ReadParams();

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--mode")
    private SaveMode mode = SaveMode.Overwrite;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.mode(mode)
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());
    }
}
