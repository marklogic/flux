/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.DelimitedFilesImporter;
import com.marklogic.flux.api.ReadTabularFilesOptions;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-delimited-files",
    abbreviateSynopsis = true,
    description = "Read delimited text files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-csv.html, with each row being written " +
    "as a JSON  or XML document to MarkLogic."
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

    public static class ReadDelimitedFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark CSV option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.ArgGroup(exclusive = false)
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putIfAbsent("header", "true");
            options.putIfAbsent("inferSchema", "true");
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadTabularFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
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
    public DelimitedFilesImporter from(Consumer<ReadTabularFilesOptions> consumer) {
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
