package com.marklogic.newtool.impl.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.ParquetFilesImporter;
import com.marklogic.newtool.api.ReadSparkFilesOptions;
import com.marklogic.newtool.api.WriteStructuredDocumentsOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read Parquet files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-parquet.html, with each row being written " +
    "as a JSON or XML document in MarkLogic.")
public class ImportParquetFilesCommand extends AbstractImportFilesCommand<ParquetFilesImporter> implements ParquetFilesImporter {

    @ParametersDelegate
    private ReadParquetFilesParams readParams = new ReadParquetFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "parquet";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    public static class ReadParquetFilesParams extends ReadFilesParams<ReadSparkFilesOptions> implements ReadSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Parquet data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadSparkFilesOptions additionalOptions(Map<String, String> options) {
            additionalOptions = options;
            return this;
        }
    }

    @Override
    public ParquetFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ParquetFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
