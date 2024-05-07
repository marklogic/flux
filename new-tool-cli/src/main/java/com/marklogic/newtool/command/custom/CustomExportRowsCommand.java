package com.marklogic.newtool.command.custom;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.export.ReadRowsParams;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

@Parameters(commandDescription = "Read rows from MarkLogic and write them using a custom Spark connector or data source.")
public class CustomExportRowsCommand extends AbstractCustomExportCommand {

    @ParametersDelegate
    private ReadRowsParams readRowsParams = new ReadRowsParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readRowsParams.makeOptions())
            .load();
    }
}