/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.ParquetFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-parquet-files",
    description = "Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3 " +
        "using Spark's support defined at %nhttps://spark.apache.org/docs/latest/sql-data-sources-parquet.html ."
)
public class ExportParquetFilesCommand extends AbstractExportRowsToFilesCommand<ParquetFilesExporter> implements ParquetFilesExporter {

    @CommandLine.Mixin
    private WriteParquetFilesParams writeParams = new WriteParquetFilesParams();

    @Override
    protected String getWriteFormat() {
        return "parquet";
    }

    @Override
    protected WriteParquetFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteParquetFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @CommandLine.Option(
            names = "--spark-prop",
            description = "Specify any Spark Parquet option defined at " +
                "%nhttps://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. --spark-prop compression=gzip."
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
    public ParquetFilesExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ParquetFilesExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public ParquetFilesExporter to(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public ParquetFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
