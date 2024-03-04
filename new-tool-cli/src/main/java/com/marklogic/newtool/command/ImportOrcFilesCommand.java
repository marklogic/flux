package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-orc.html, with each row being " +
    "written to MarkLogic.")
public class ImportOrcFilesCommand extends AbstractImportFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark ORC option or configuration item defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -PmergeSchema=true or " +
            "-Pspark.sql.orc.filterPushdown=false."
    )
    private Map<String, String> orcParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "orc";
    }

    @Override
    protected void modifySparkSession(SparkSession session) {
        orcParams.entrySet().stream()
            .filter(OptionsUtil::isSparkConfigurationOption)
            .forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        orcParams.entrySet().stream()
            .filter(OptionsUtil::isSparkDataSourceOption)
            .forEach(entry -> options.put(entry.getKey(), entry.getValue()));
        return options;
    }
}
