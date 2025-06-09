/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.AvroFilesImporter;
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
    name = "import-avro-files",
    description = "Read Avro files from local, HDFS, and S3 locations using Spark's support defined at" +
        "%nhttps://spark.apache.org/docs/latest/sql-data-sources-avro.html, and write JSON or XML documents " +
        "to MarkLogic."
)
public class ImportAvroFilesCommand extends AbstractImportFilesCommand<AvroFilesImporter> implements AvroFilesImporter {

    @CommandLine.Mixin
    private ReadAvroFilesParams readParams = new ReadAvroFilesParams();

    @CommandLine.Mixin
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "avro";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    public static class ReadAvroFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark Avro data source option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true. " +
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

        TdeHelper tdeHelper = new TdeHelper(writeParams.getTdeParams(), writeParams.getJsonRootName(), writeParams.getXmlRootName(), writeParams.getXmlNamespace());
        if (tdeHelper.logTemplateIfNecessary(dataset.schema())) {
            return null;
        }
        tdeHelper.loadTemplateIfNecessary(dataset.schema(), getConnectionParams());

        return dataset;
    }

    @Override
    public AvroFilesImporter from(Consumer<ReadTabularFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AvroFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public AvroFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
