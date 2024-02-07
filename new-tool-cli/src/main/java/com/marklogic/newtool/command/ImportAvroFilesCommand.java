package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "to MarkLogic.")
public class ImportAvroFilesCommand extends AbstractImportFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true."
    )
    private Map<String, String> avroParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "avro";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putAll(avroParams);
        return options;
    }
}
