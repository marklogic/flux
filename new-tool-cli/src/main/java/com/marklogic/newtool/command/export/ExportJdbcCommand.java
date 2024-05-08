package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.Executor;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.JdbcParams;
import com.marklogic.newtool.command.OptionsUtil;
import org.apache.spark.sql.*;

import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to a table via JDBC.")
public class ExportJdbcCommand extends AbstractCommand<Executor<ExportJdbcCommand>> {

    @ParametersDelegate
    private ReadRowsParams readRowsParams = new ReadRowsParams();

    @ParametersDelegate
    private WriteJdbcParams writeParams = new WriteJdbcParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readRowsParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format("jdbc")
            .options(writeParams.makeOptions())
            .mode(writeParams.saveMode)
            .save();
    }

    public static class WriteJdbcParams extends JdbcParams {

        @Parameter(names = "--table", required = true, description = "The JDBC table that should be written to.")
        private String table;

        @Parameter(names = "--mode", converter = SaveModeConverter.class,
            description = "Specifies how data is written to a table if the table already exists. " +
                "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
        private SaveMode saveMode = SaveMode.ErrorIfExists;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                "dbtable", table
            );
        }
    }
}
