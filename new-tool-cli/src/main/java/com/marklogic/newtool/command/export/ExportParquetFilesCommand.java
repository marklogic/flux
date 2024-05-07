package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3.")
public class ExportParquetFilesCommand extends AbstractExportRowsToFilesCommand {

    @ParametersDelegate
    private WriteParquetFilesParams writeParams = new WriteParquetFilesParams();

    @Override
    protected String getWriteFormat() {
        return "parquet";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteParquetFilesParams extends WriteStructuredFilesParams {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Parquet option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -Pcompression=gzip."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return dynamicParams;
        }
    }
}
