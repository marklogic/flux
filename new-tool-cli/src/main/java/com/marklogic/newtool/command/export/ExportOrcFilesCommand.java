package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to ORC files on a local filesystem, HDFS, or S3.")
public class ExportOrcFilesCommand extends AbstractExportRowsToFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark ORC option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -Pcompression=lz4."
    )
    private Map<String, String> orcParams = new HashMap<>();

    @Override
    protected String getWriteFormat() {
        return "orc";
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        return orcParams;
    }
}
