/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.AggregateJsonFilesImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-aggregate-json-files",
    description = "Read either JSON Lines files or files containing arrays of JSON objects from " +
        "local, HDFS, and S3 locations using Spark's support " +
        "defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-json.html, with each object being written " +
        "as a JSON document to MarkLogic."
)
public class ImportAggregateJsonFilesCommand extends AbstractImportFilesCommand<AggregateJsonFilesImporter> implements AggregateJsonFilesImporter {

    @CommandLine.Mixin
    private ReadJsonFilesParams readParams = new ReadJsonFilesParams();

    @CommandLine.Mixin
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return readParams.jsonLinesRaw ? MARKLOGIC_CONNECTOR : "json";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    public static class ReadJsonFilesParams extends ReadFilesParams<ReadJsonFilesOptions> implements ReadJsonFilesOptions {

        @CommandLine.Option(
            names = "--json-lines",
            description = "Specifies that the file contains one JSON object per line, per the JSON Lines format defined at https://jsonlines.org/ , " +
                "using the Spark JSON data source defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-json.html."
        )
        private boolean jsonLines;

        @CommandLine.Option(
            names = "--json-lines-raw",
            description = "Specifies that the file contains one JSON object per line, per the JSON Lines format defined at https://jsonlines.org/ , " +
                "and that each line should be read as is without any schema enforced across the lines."
        )
        private boolean jsonLinesRaw;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @CommandLine.Option(
            names = "--uri-include-file-path",
            description = "If true, each document URI will include the path of the originating file."
        )
        private boolean uriIncludeFilePath;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark JSON option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-json.html; e.g. -PallowComments=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            if (jsonLinesRaw) {
                options.put(Options.READ_FILES_TYPE, "json_lines");
            } else if (!jsonLines) {
                // Spark JSON defaults to JSON Lines format. This commands assumes the opposite, so it defaults to including
                // multiLine=true (i.e. not JSON Lines) unless the user has included the option requesting JSON Lines support.
                options.put("multiLine", "true");
            }
            if (encoding != null) {
                options.put("encoding", encoding);
            }
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        // Sonar complains about this method existing, but it has to exist since it's implementing a required
        // method in the interface.
        @SuppressWarnings("java:S5738")
        public ReadJsonFilesOptions jsonLines(boolean value) {
            this.jsonLines = value;
            return this;
        }

        @Override
        public ReadJsonFilesOptions jsonLines() {
            this.jsonLines = true;
            return this;
        }

        @Override
        public ReadJsonFilesOptions jsonLinesRaw() {
            this.jsonLinesRaw = true;
            return this;
        }

        @Override
        public ReadJsonFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public ReadJsonFilesOptions uriIncludeFilePath(boolean value) {
            this.uriIncludeFilePath = value;
            return this;
        }

        @Override
        public ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        TdeHelper tdeHelper = new TdeHelper(writeParams.getTdeParams(), writeParams.getJsonRootName(), writeParams.getXmlRootName(), writeParams.getXmlNamespace());
        if (tdeHelper.logTemplateIfNecessary(dataset.schema())) {
            return null;
        }
        tdeHelper.loadTemplateIfNecessary(dataset.schema(), getConnectionParams());
        
        // If jsonLinesRaw, the MarkLogic connector is used, in which case the Spark file path column will not be present.
        if (readParams.uriIncludeFilePath && !readParams.jsonLinesRaw) {
            dataset = SparkUtil.addFilePathColumn(dataset);
        }
        return dataset;
    }

    @Override
    public AggregateJsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AggregateJsonFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public AggregateJsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
