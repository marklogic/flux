package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-orc.html, with each row being " +
    "written as a JSON or XML document in MarkLogic.")
public class ImportOrcFilesCommand extends AbstractImportFilesCommand<ImportOrcFilesCommand> {

    @ParametersDelegate
    private ReadOrcFilesParams readParams = new ReadOrcFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeDocumentParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "orc";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeDocumentParams;
    }

    public static class ReadOrcFilesParams extends ReadFilesParams<ReadOrcFilesParams> {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark ORC data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -PmergeSchema=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putAll(dynamicParams);
            return options;
        }
    }
}
