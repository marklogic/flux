/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.OrcFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-orc-files",
    abbreviateSynopsis = true,
    description = "Read rows via Optic from MarkLogic and write them to ORC files on a local filesystem, HDFS, or S3."
)
public class ExportOrcFilesCommand extends AbstractExportRowsToFilesCommand<OrcFilesExporter> implements OrcFilesExporter {

    @CommandLine.ArgGroup(exclusive = false, heading = WRITER_OPTIONS_HEADING)
    private WriteOrcFilesParams writeParams = new WriteOrcFilesParams();

    @Override
    protected String getWriteFormat() {
        return "orc";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteOrcFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark ORC option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-orc.html; e.g. -Pcompression=lz4."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return additionalOptions;
        }

        @Override
        public WriteSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }
    }

    @Override
    public OrcFilesExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public OrcFilesExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public OrcFilesExporter to(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public OrcFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
