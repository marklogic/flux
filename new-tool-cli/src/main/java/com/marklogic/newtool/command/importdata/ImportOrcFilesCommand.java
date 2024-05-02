package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-orc.html, with each row being " +
    "written as a JSON document in MarkLogic.")
public class ImportOrcFilesCommand extends AbstractImportStructuredFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark ORC option or configuration item defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -PmergeSchema=true or " +
            "-Pspark.sql.orc.filterPushdown=false."
    )
    private Map<String, String> orcParams;

    public ImportOrcFilesCommand() {
        super("orc");
        orcParams = new HashMap<>();
        setDynamicParams(orcParams);
    }
}
