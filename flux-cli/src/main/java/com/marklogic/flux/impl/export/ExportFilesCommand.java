/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.CompressionType;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.GenericFilesExporter;
import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.S3Params;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@CommandLine.Command(
    name = "export-files",
    description = "Read documents from MarkLogic and write them to a local filesystem, HDFS, or S3."
)
public class ExportFilesCommand extends AbstractCommand<GenericFilesExporter> implements GenericFilesExporter {

    @CommandLine.Mixin
    private ReadDocumentParamsImpl readParams = new ReadDocumentParamsImpl();

    @CommandLine.Mixin
    protected WriteGenericFilesParams writeParams = new WriteGenericFilesParams();

    @Override
    protected void validateDuringApiUsage() {
        writeParams.validatePath();
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        super.validateCommandLineOptions(parseResult);
        OptionsUtil.verifyHasAtLeastOneOption(parseResult, ReadDocumentParams.REQUIRED_QUERY_OPTIONS);
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        final Integer zipFileCount = writeParams.zipFileCount;
        if (zipFileCount != null && zipFileCount > 0) {
            getCommonParams().setRepartition(zipFileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writeParams.s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(MARKLOGIC_CONNECTOR)
            .options(writeParams.get())
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite files.
            .mode(SaveMode.Append)
            .save(writeParams.path);
    }

    public static class WriteGenericFilesParams implements Supplier<Map<String, String>>, WriteGenericFilesOptions {

        @CommandLine.Option(required = true, names = "--path", description = "Path expression for where files should be written.")
        private String path;

        @CommandLine.Mixin
        private S3Params s3Params = new S3Params();

        @CommandLine.Option(names = "--compression", description = "Set to 'ZIP' to write one zip file per partition, " +
            "or to 'GZIP' to GZIP each document file. " + OptionsUtil.VALID_VALUES_DESCRIPTION)
        private CompressionType compressionType;

        @CommandLine.Option(names = "--pretty-print", description = "Pretty-print the contents of JSON and XML files.")
        private Boolean prettyPrint;

        @CommandLine.Option(names = "--zip-file-count", description = "Specifies how many ZIP files should be written when --compression is set to 'ZIP'; also an alias for '--repartition'.")
        private Integer zipFileCount;

        @Override
        public Map<String, String> get() {
            return OptionsUtil.makeOptions(
                Options.WRITE_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null,
                Options.WRITE_FILES_PRETTY_PRINT, prettyPrint != null ? prettyPrint.toString() : null
            );
        }

        public void validatePath() {
            if (path == null || path.trim().length() == 0) {
                throw new FluxException("Must specify a file path");
            }
        }

        @Override
        public WriteGenericFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Override
        public WriteGenericFilesOptions prettyPrint(Boolean value) {
            this.prettyPrint = value;
            return this;
        }

        @Override
        public WriteGenericFilesOptions zipFileCount(Integer zipFileCount) {
            this.zipFileCount = zipFileCount;
            return this;
        }

        @Override
        public WriteGenericFilesOptions path(String path) {
            this.path = path;
            return this;
        }

        @Override
        public WriteGenericFilesOptions s3AddCredentials() {
            s3Params.setAddCredentials(true);
            return this;
        }

        @Override
        public WriteGenericFilesOptions s3AccessKeyId(String accessKeyId) {
            s3Params.setAccessKeyId(accessKeyId);
            return this;
        }

        @Override
        public WriteGenericFilesOptions s3SecretAccessKey(String secretAccessKey) {
            s3Params.setSecretAccessKey(secretAccessKey);
            return this;
        }

        @Override
        public WriteGenericFilesOptions s3Endpoint(String endpoint) {
            s3Params.setEndpoint(endpoint);
            return this;
        }
    }

    @Override
    public GenericFilesExporter from(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public GenericFilesExporter to(Consumer<WriteGenericFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public GenericFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
