package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "to MarkLogic.")
public class ImportAvroFilesCommand extends AbstractImportStructuredFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro option or configuration item defined at" +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true or " +
            "-Pspark.sql.avro.filterPushdown.enabled=false."
    )
    private Map<String, String> avroParams;

    public ImportAvroFilesCommand() {
        super("avro");
        this.avroParams = new HashMap<>();
        setDynamicParams(this.avroParams);
    }
}
