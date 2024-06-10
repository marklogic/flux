package com.marklogic.newtool.impl.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.CompressionType;
import com.marklogic.newtool.api.GenericFilesExporter;
import com.marklogic.newtool.api.NtException;
import com.marklogic.newtool.api.ReadDocumentsOptions;
import com.marklogic.newtool.impl.AbstractCommand;
import com.marklogic.newtool.impl.OptionsUtil;
import com.marklogic.newtool.impl.S3Params;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Parameters(commandDescription = "Read documents from MarkLogic and write them to a local filesystem, HDFS, or S3.")
public class ExportFilesCommand extends AbstractCommand<GenericFilesExporter> implements GenericFilesExporter {

    @ParametersDelegate
    private ReadDocumentParamsImpl readParams = new ReadDocumentParamsImpl();

    @ParametersDelegate
    protected WriteGenericFilesParams writeParams = new WriteGenericFilesParams();

    @Override
    protected void validateDuringApiUsage() {
        writeParams.validatePath();
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

        @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
        private String path;

        @ParametersDelegate
        private S3Params s3Params = new S3Params();

        @Parameter(names = "--compression", description = "Set to 'ZIP' to write one zip file per partition, or to 'GZIP' to GZIP each document file.")
        private CompressionType compressionType;

        @Parameter(names = "--pretty-print", description = "Pretty-print the contents of JSON and XML files.")
        private Boolean prettyPrint;

        @Parameter(names = "--zip-file-count", description = "Specifies how many ZIP files should be written when --compression is set to 'ZIP'; also an alias for '--repartition'.")
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
                throw new NtException("Must specify a file path");
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
        public WriteGenericFilesOptions s3Endpoint(String endpoint) {
            s3Params.setEndpoint(endpoint);
            return this;
        }
    }

    @Override
    public GenericFilesExporter readDocuments(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public GenericFilesExporter writeFiles(Consumer<WriteGenericFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public GenericFilesExporter writeFiles(String path) {
        writeParams.path(path);
        return this;
    }
}
