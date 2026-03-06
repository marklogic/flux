/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ParquetFilesImporter;
import com.marklogic.flux.api.ReadTabularFilesOptions;
import com.marklogic.flux.api.StructuredDataImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.flux.impl.TdeHelper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-parquet-files",
    description = "Read Parquet files from supported file locations using Spark's support " +
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
    protected IReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteStructuredDocumentParams getWriteParams() {
        return writeParams;
    }

    public static class ReadParquetFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "--spark-prop",
            description = "Specify any Spark Parquet data source option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. --spark-prop mergeSchema=true. " +
                "Spark configuration options must be defined via '--spark-conf'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.Mixin
        private StructuredDataParams structuredDataParams = new StructuredDataParams();

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
        @Deprecated
        public ReadTabularFilesOptions groupBy(String columnName) {
            structuredDataParams.setGroupBy(columnName);
            return this;
        }

        @Override
        @Deprecated
        public ReadTabularFilesOptions aggregateColumns(String aggregationName, String... columns) {
            structuredDataParams.aggregateColumns(aggregationName, columns);
            return this;
        }

        @Override
        @Deprecated
        public ReadTabularFilesOptions orderAggregation(String aggregationName, String columnName, boolean ascending) {
            structuredDataParams.orderAggregation(aggregationName, columnName, ascending);
            return this;
        }

        @Override
        public ReadTabularFilesOptions uriIncludeFilePath(boolean value) {
            this.uriIncludeFilePath = value;
            return this;
        }

        @Override
        public ReadTabularFilesOptions drop(String... columns) {
            structuredDataParams.drop(columns);
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        if (readParams.uriIncludeFilePath) {
            dataset = SparkUtil.addFilePathColumn(dataset);
        }

        dataset = readParams.structuredDataParams.applyTransformations(dataset);

        TdeHelper.Result result = writeParams.newTdeHelper().logOrLoadTemplate(dataset.schema(), getConnectionParams());
        if (TdeHelper.Result.TEMPLATE_LOGGED.equals(result)) {
            return null;
        }

        return dataset;
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

    @Override
    public ParquetFilesImporter where(String expression) {
        readParams.structuredDataParams.where(expression);
        return this;
    }

    @Override
    public ParquetFilesImporter groupBy(String columnName, Consumer<StructuredDataImporter.GroupByOptions<?>> consumer) {
        readParams.structuredDataParams.setGroupBy(columnName);
        consumer.accept(readParams.structuredDataParams);
        return this;
    }
}
