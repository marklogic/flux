/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.DelimitedFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-delimited-files",
    description = "Read rows via Optic from MarkLogic and write them to delimited text files on a local filesystem, " +
        "HDFS, or S3 using Spark's support defined at %nhttps://spark.apache.org/docs/3.5.6/sql-data-sources-csv.html ."
)
public class ExportDelimitedFilesCommand extends AbstractExportRowsToFilesCommand<DelimitedFilesExporter> implements DelimitedFilesExporter {

    @CommandLine.Mixin
    private WriteDelimitedFilesParams writeParams = new WriteDelimitedFilesParams();

    @Override
    protected String getWriteFormat() {
        return "csv";
    }

    @Override
    protected WriteDelimitedFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteDelimitedFilesParams extends WriteStructuredFilesParams<WriteDelimitedFilesOptions> implements WriteDelimitedFilesOptions {

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding for writing files.")
        private String encoding;

        @CommandLine.Option(
            names = {"-P"},
            description = "Specify any Spark CSV option defined at %nhttps://spark.apache.org/docs/3.5.6/sql-data-sources-csv.html; e.g. -PquoteAll=true."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            Map<String, String> options = OptionsUtil.makeOptions("header", "true");
            if (encoding != null) {
                options.put("encoding", encoding);
            }
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public WriteDelimitedFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public WriteDelimitedFilesOptions additionalOptions(Map<String, String> options) {
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
    public DelimitedFilesExporter to(Consumer<WriteDelimitedFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public DelimitedFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
