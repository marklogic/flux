package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

@Parameters(commandDescription = "Read rows via JDBC and write documents in MarkLogic.")
public class ImportJdbcCommand extends AbstractCommand {

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--query", required = true,
        description = "A SQL query used to read data from the JDBC data source.")
    private String query;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return session.read().format("jdbc")
            .options(jdbcParams.makeOptions())
            .option("query", query)
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeDocumentParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }
}
