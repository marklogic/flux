package com.marklogic.flux.impl.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.ParquetFilesImporter;
import com.marklogic.flux.api.ReadTabularFilesOptions;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

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

    public static class ReadParquetFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Parquet data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @ParametersDelegate
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadTabularFilesOptions additionalOptions(Map<String, String> options) {
            additionalOptions = options;
            return this;
        }

        @Override
        public ReadTabularFilesOptions groupBy(String columnName) {
            aggregationParams.setGroupBy(columnName);
            return this;
        }

        @Override
        public ReadTabularFilesOptions aggregateColumns(String newColumnName, String... columns) {
            aggregationParams.addAggregationExpression(newColumnName, columns);
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        return readParams.aggregationParams.applyGroupBy(dataset);
    }

    @Override
    public ParquetFilesImporter from(Consumer<ReadTabularFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ParquetFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public ParquetFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}