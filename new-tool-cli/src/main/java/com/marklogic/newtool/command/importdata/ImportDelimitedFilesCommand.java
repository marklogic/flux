package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read delimited text files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-csv.html, with each row being written " +
    "as a JSON  or XML document to MarkLogic.")
public class ImportDelimitedFilesCommand extends AbstractImportFilesCommand<ImportDelimitedFilesCommand> {

    @ParametersDelegate
    private ReadDelimitedFilesParams readParams = new ReadDelimitedFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "csv";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    public static class ReadDelimitedFilesParams extends ReadFilesParams<ReadDelimitedFilesParams> {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark CSV option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putIfAbsent("header", "true");
            options.putIfAbsent("inferSchema", "true");
            options.putAll(dynamicParams);
            return options;
        }
    }
}
