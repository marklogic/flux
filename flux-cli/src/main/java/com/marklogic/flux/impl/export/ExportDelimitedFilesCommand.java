/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.DelimitedFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;
import com.marklogic.flux.impl.OptionsUtil;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-delimited-files",
    abbreviateSynopsis = true,
    description = "Read rows via Optic from MarkLogic and write them to delimited text files on a local filesystem, HDFS, or S3."
)
public class ExportDelimitedFilesCommand extends AbstractExportRowsToFilesCommand<DelimitedFilesExporter> implements DelimitedFilesExporter {

    @CommandLine.ArgGroup(exclusive = false, heading = WRITER_OPTIONS_HEADING)
    private WriteDelimitedFilesParams writeParams = new WriteDelimitedFilesParams();

    @Override
    protected String getWriteFormat() {
        return "csv";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteDelimitedFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @CommandLine.Option(
            names = {"-P"},
            description = "Specify any Spark CSV option defined at https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            Map<String, String> options = OptionsUtil.makeOptions("header", "true");
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public WriteSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }
    }

    @Override
    public DelimitedFilesExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public DelimitedFilesExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public DelimitedFilesExporter to(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public DelimitedFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
