/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.DelimitedFilesImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.SparkUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-delimited-files",
    description = "Read delimited text files from local, HDFS, and S3 locations using Spark's support " +
        "defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-csv.html, and write JSON or XML documents " +
        "to MarkLogic."
)
public class ImportDelimitedFilesCommand extends AbstractImportFilesCommand<DelimitedFilesImporter> implements DelimitedFilesImporter {

    @CommandLine.Mixin
    private ReadDelimitedFilesParams readParams = new ReadDelimitedFilesParams();

    @CommandLine.Mixin
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

    public static class ReadDelimitedFilesParams extends ReadFilesParams<ReadDelimitedFilesOptions> implements ReadDelimitedFilesOptions {

        @CommandLine.Option(names = "--delimiter", description = "Specify a delimiter; defaults to a comma.")
        private String delimiter;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark CSV option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.Mixin
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putIfAbsent("header", "true");
            options.putIfAbsent("inferSchema", "true");
            if (delimiter != null) {
                options.putIfAbsent("sep", delimiter);
            }
            if (encoding != null) {
                options.putIfAbsent("encoding", encoding);
            }
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadDelimitedFilesOptions delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        @Override
        public ReadDelimitedFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public ReadDelimitedFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }

        @Override
        public ReadDelimitedFilesOptions uriIncludeFilePath(boolean value) {
            this.uriIncludeFilePath = value;
            return this;
        }

        @Override
        public ReadDelimitedFilesOptions groupBy(String columnName) {
            aggregationParams.setGroupBy(columnName);
            return this;
        }

        @Override
        public ReadDelimitedFilesOptions aggregateColumns(String newColumnName, String... columns) {
            aggregationParams.addAggregationExpression(newColumnName, columns);
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        if (readParams.uriIncludeFilePath) {
            dataset = SparkUtil.addFilePathColumn(dataset);
        }

        dataset = readParams.aggregationParams.applyGroupBy(dataset);

        if (writeParams.generateTde(dataset.schema(), getConnectionParams())) {
            return null;
        }

        return dataset;
    }

    @Override
    public DelimitedFilesImporter from(Consumer<ReadDelimitedFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public DelimitedFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public DelimitedFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
