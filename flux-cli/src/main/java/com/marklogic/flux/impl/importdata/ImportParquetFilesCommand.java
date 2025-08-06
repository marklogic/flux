/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ParquetFilesImporter;
import com.marklogic.flux.api.ReadTabularFilesOptions;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.SparkUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-parquet-files",
    description = "Read Parquet files from local, HDFS, and S3 locations using Spark's support " +
        "defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-parquet.html, and write JSON or XML " +
        "documents to MarkLogic."
)
public class ImportParquetFilesCommand extends AbstractImportFilesCommand<ParquetFilesImporter> implements ParquetFilesImporter {

    @CommandLine.Mixin
    private ReadParquetFilesParams readParams = new ReadParquetFilesParams();

    @CommandLine.Mixin
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

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark Parquet data source option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.Mixin
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

        @Override
        public ReadTabularFilesOptions uriIncludeFilePath(boolean value) {
            this.uriIncludeFilePath = value;
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        if (readParams.uriIncludeFilePath) {
            dataset = SparkUtil.addFilePathColumn(dataset);
        }
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
