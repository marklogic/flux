package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Avro files on a local filesystem, HDFS, or S3.")
public class ExportAvroFilesCommand extends AbstractExportRowsToFilesCommand {

    @ParametersDelegate
    private WriteAvroFilesParams writeParams = new WriteAvroFilesParams();

    @Override
    protected String getWriteFormat() {
        return "avro";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteAvroFilesParams extends WriteStructuredFilesParams {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Avro option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -Pcompression=bzip2."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return dynamicParams;
        }
    }
}
