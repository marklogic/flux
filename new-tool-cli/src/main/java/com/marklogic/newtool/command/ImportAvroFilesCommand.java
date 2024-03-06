package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "to MarkLogic.")
public class ImportAvroFilesCommand extends AbstractImportFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro option or configuration item defined at" +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true or " +
            "-Pspark.sql.avro.filterPushdown.enabled=false."
    )
    private Map<String, String> avroParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "avro";
    }

    @Override
    protected void modifySparkSession(SparkSession session) {
        avroParams.entrySet().stream()
            .filter(OptionsUtil::isSparkConfigurationOption)
            .forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        avroParams.entrySet().stream()
            .filter(OptionsUtil::isSparkDataSourceOption)
            .forEach(entry -> options.put(entry.getKey(), entry.getValue()));
        return options;
    }
}
