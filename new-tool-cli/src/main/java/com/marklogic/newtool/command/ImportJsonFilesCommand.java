package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read JSON files, including JSON Lines files, from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-json.html , with each object being written " +
    "as a JSON document in MarkLogic.")
public class ImportJsonFilesCommand extends AbstractImportFilesCommand {

    @Parameter(
        names = "--jsonLines",
        description = "Specifies that the file contains one JSON object per line, per the JSON Lines format defined at https://jsonlines.org/ ."
    )
    private Boolean jsonLines;

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

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
        // Spark JSON defaults to JSON Lines format. This commands assumes the opposite, so it defaults to including
        // multiLine=true (i.e. not JSON Lines) unless the user has included the option requesting JSON Lines support.
        if (jsonLines == null || !jsonLines.booleanValue()) {
            options.put("multiLine", "true");
        }
        options.putAll(jsonParams);
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        return OptionsUtil.addOptions(super.makeWriteOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName
        );
    }
}
