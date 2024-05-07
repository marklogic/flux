package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Parquet files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-parquet.html, with each row being written " +
    "as a JSON or XML document in MarkLogic.")
public class ImportParquetFilesCommand extends AbstractImportFilesCommand {

    @ParametersDelegate
    private ReadParquetFilesParams readParams = new ReadParquetFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeDocumentParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "parquet";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentWithTemplateParams getWriteParams() {
        return writeDocumentParams;
    }

    public static class ReadParquetFilesParams extends ReadFilesParams {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Parquet data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true. " +
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
