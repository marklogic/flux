package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3.")
public class ExportParquetFilesCommand extends AbstractExportRowsToFilesCommand {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Parquet option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -Pcompression=gzip."
    )
    private Map<String, String> parquetParams = new HashMap<>();

    @Override
    protected String getWriteFormat() {
        return "parquet";
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        return parquetParams;
    }
}
