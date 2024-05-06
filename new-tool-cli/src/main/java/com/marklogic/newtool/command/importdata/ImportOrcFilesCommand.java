package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.api.OrcFilesImporter;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-orc.html, with each row being " +
    "written as a JSON or XML document in MarkLogic.")
public class ImportOrcFilesCommand extends AbstractImportStructuredFilesCommand<OrcFilesImporter> implements OrcFilesImporter {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark ORC data source option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -PmergeSchema=true. " +
            "Spark configuration options must be defined via '-C'."
    )
    private Map<String, String> orcParams;

    public ImportOrcFilesCommand() {
        super("orc");
        orcParams = new HashMap<>();
        setDynamicParams(orcParams);
    }
}
