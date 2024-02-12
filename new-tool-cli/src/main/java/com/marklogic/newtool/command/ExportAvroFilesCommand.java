package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Avro files on a local filesystem, HDFS, or S3.")
public class ExportAvroFilesCommand extends AbstractExportFilesCommand {
    
    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -Pcompression=bzip2."
    )
    private Map<String, String> avroParams = new HashMap<>();

    @Override
    protected String getWriteFormat() {
        return "avro";
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        return avroParams;
    }
}
