package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to ORC files on a local filesystem, HDFS, or S3.")
public class ExportOrcFilesCommand extends AbstractExportRowsToFilesCommand {

    @ParametersDelegate
    private WriteOrcFilesParams writeParams = new WriteOrcFilesParams();

    @Override
    protected String getWriteFormat() {
        return "orc";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteOrcFilesParams extends WriteStructuredFilesParams {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark ORC option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -Pcompression=lz4."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return dynamicParams;
        }
    }
}
