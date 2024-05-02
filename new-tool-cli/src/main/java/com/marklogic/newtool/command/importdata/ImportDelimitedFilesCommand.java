package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read delimited text files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-csv.html, with each row being written " +
    "as a JSON  or XML document to MarkLogic.")
public class ImportDelimitedFilesCommand extends AbstractImportStructuredFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark CSV option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
    )
    private Map<String, String> csvParams;

    public ImportDelimitedFilesCommand() {
        super("csv");
        csvParams = new HashMap<>();
        setDynamicParams(csvParams);
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putIfAbsent("header", "true");
        options.putIfAbsent("inferSchema", "true");
        return options;
    }
}
