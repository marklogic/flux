/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.JsonFilesImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read JSON files, including JSON Lines files, from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-json.html , with each object being written " +
    "as a JSON document in MarkLogic.")
public class ImportJsonFilesCommand extends AbstractImportFilesCommand<JsonFilesImporter> implements JsonFilesImporter {

    @ParametersDelegate
    private ReadJsonFilesParams readParams = new ReadJsonFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "json";
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

        @Parameter(
            names = "--json-lines",
            description = "Specifies that the file contains one JSON object per line, per the JSON Lines format defined at https://jsonlines.org/ ."
        )
        private Boolean jsonLines;

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark JSON option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-json.html; e.g. -PallowComments=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            // Spark JSON defaults to JSON Lines format. This commands assumes the opposite, so it defaults to including
            // multiLine=true (i.e. not JSON Lines) unless the user has included the option requesting JSON Lines support.
            if (jsonLines == null || !jsonLines.booleanValue()) {
                options.put("multiLine", "true");
            }
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadJsonFilesOptions jsonLines(Boolean value) {
            this.jsonLines = value;
            return this;
        }

        @Override
        public ReadJsonFilesOptions additionalOptions(Map<String, String> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }
    }

    @Override
    public JsonFilesImporter from(Consumer<ReadJsonFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JsonFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public JsonFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
