/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.AvroFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-avro-files",
    description = "Read rows via Optic from MarkLogic and write them to Avro files on a local filesystem, HDFS, or S3 " +
        "using Spark's support defined at %nhttps://spark.apache.org/docs/3.5.6/sql-data-sources-avro.html ."
)
public class ExportAvroFilesCommand extends AbstractExportRowsToFilesCommand<AvroFilesExporter> implements AvroFilesExporter {

    @CommandLine.Mixin
    private WriteAvroFilesParams writeParams = new WriteAvroFilesParams();

    @Override
    protected String getWriteFormat() {
        return "avro";
    }

    @Override
    protected WriteAvroFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteAvroFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @CommandLine.Option(
            names = "--spark-prop",
            description = "Specify any Spark Avro option defined at " +
                "%nhttps://spark.apache.org/docs/3.5.6/sql-data-sources-avro.html; e.g. --spark-prop compression=bzip2."
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
    public AvroFilesExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AvroFilesExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public AvroFilesExporter to(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public AvroFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
