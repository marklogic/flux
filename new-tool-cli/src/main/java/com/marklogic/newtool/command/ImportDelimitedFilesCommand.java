package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read delimited text files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-csv.html, with each row being written " +
    "as a JSON document to MarkLogic.")
public class ImportDelimitedFilesCommand extends AbstractImportFilesCommand {

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @ParametersDelegate
    private XmlDocumentParams xmlDocumentParams = new XmlDocumentParams();

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark CSV option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
    )
    private Map<String, String> csvParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "csv";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        options.putAll(csvParams);
        if (!options.containsKey("header")) {
            options.put("header", "true");
        }
        if (!options.containsKey("inferSchema")) {
            options.put("inferSchema", "true");
        }
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = OptionsUtil.addOptions(super.makeWriteOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName
        );
        options.putAll(xmlDocumentParams.makeOptions());
        return options;
    }
}
