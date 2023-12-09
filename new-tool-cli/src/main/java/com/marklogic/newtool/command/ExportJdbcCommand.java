package com.marklogic.newtool.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Optional;

@Parameters(commandDescription = "Read rows via Optic and write via JDBC")
public class ExportJdbcCommand extends AbstractExportCommand {

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        session.read()
            .format(MARKLOGIC_CONNECTOR)
            .options(getReadParams().makeOptions())
            .load()
            .write()
            .mode(SaveMode.Overwrite)
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());
        return Optional.empty();
    }
}
