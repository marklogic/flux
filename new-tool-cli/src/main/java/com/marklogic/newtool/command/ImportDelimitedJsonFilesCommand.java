package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read delimited JSON lines files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-json.html , with each row being written " +
    "to MarkLogic.")
public class ImportDelimitedJsonFilesCommand extends AbstractImportFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark JSON option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-json.html; e.g. -PallowComments=true."
    )
    private Map<String, String> jsonParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "json";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putAll(jsonParams);
        return options;
    }
}
