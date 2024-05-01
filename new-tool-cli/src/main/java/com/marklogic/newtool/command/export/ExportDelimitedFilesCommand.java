package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.command.OptionsUtil;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to delimited text files on a local filesystem, HDFS, or S3.")
public class ExportDelimitedFilesCommand extends AbstractExportRowsToFilesCommand {

    @DynamicParameter(names = "-P", description = "Specify any Spark CSV option defined at " + "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true.")
    private Map<String, String> csvParams = new HashMap<>();

    @Override
    protected String getWriteFormat() {
        return "csv";
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = OptionsUtil.makeOptions("header", "true");
        options.putAll(csvParams);
        return options;
    }
}
