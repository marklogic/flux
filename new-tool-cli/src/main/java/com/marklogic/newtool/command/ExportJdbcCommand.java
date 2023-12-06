package com.marklogic.newtool.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

@Parameters(commandDescription = "Read rows via Optic and write via JDBC")
public class ExportJdbcCommand extends AbstractExportCommand {

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Override
    public void execute(SparkSession session) {
        read(session).write()
            .mode(SaveMode.Overwrite)
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());
    }
}
