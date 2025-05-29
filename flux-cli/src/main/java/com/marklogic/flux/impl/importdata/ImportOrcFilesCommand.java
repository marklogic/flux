/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.OrcFilesImporter;
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
    name = "import-orc-files",
    description = "Read ORC files from local, HDFS, and S3 locations using Spark's support " +
        "defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-orc.html, and write JSON or " +
        "XML documents to MarkLogic."
)
public class ImportOrcFilesCommand extends AbstractImportFilesCommand<OrcFilesImporter> implements OrcFilesImporter {

    @CommandLine.Mixin
    private ReadOrcFilesParams readParams = new ReadOrcFilesParams();

    @CommandLine.Mixin
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

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
        return writeParams;
    }

    public static class ReadOrcFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark ORC data source option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -PmergeSchema=true. " +
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

        dataset = readParams.aggregationParams.applyGroupBy(dataset);

        if (writeParams.generateTde(dataset.schema())) {
            return null;
        }

        return dataset;
    }

    @Override
    public OrcFilesImporter from(Consumer<ReadTabularFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public OrcFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public OrcFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
