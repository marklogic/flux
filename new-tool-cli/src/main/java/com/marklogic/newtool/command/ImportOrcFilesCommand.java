package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
    "with each row being written to MarkLogic.")
public class ImportOrcFilesCommand extends AbstractImportFilesCommand{
    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark ORC option defined at "  +
                      "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -Pspark.sql.orc.filterPushdown=true."
    )
    private Map<String, String> orcParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "orc";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putAll(orcParams);
        return options;
    }
}
