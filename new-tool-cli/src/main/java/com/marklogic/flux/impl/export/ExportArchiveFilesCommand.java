package com.marklogic.flux.impl.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.api.ArchiveFilesExporter;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Parameters(commandDescription = "Read documents and their metadata from MarkLogic and write them to ZIP files on a local filesystem, HDFS, or S3.")
public class ExportArchiveFilesCommand extends AbstractCommand<ArchiveFilesExporter> implements ArchiveFilesExporter {

    @ParametersDelegate
    private ReadArchiveDocumentsParams readParams = new ReadArchiveDocumentsParams();

    @ParametersDelegate
    private WriteArchiveFilesParams writeParams = new WriteArchiveFilesParams();

    @Override
    protected void validateDuringApiUsage() {
        writeParams.validatePath();
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        final Integer fileCount = writeParams.getFileCount();
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writeParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(MARKLOGIC_CONNECTOR)
            .options(writeParams.get())
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite files.
            .mode(SaveMode.Append)
            .save(writeParams.getPath());
    }

    public static class WriteArchiveFilesParams extends WriteFilesParams<WriteArchiveFilesParams> {

        @Override
        public Map<String, String> get() {
            return OptionsUtil.makeOptions(Options.WRITE_FILES_COMPRESSION, "zip");
        }
    }

    public static class ReadArchiveDocumentsParams extends ReadDocumentParams<ReadArchiveDocumentOptions> implements ReadArchiveDocumentOptions {

        @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of data to include. " +
            "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
        private String categories;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_DOCUMENTS_CATEGORIES, determineCategories()
            );
        }

        @Override
        public ReadArchiveDocumentOptions categories(String... categories) {
            this.categories = Stream.of(categories).collect(Collectors.joining(","));
            return this;
        }

        /**
         * While the "read documents" operation allows for only reading metadata, that isn't valid for an archive - we
         * always need content to be returned as well.
         *
         * @return
         */
        private String determineCategories() {
            if (categories != null && categories.trim().length() > 0) {
                return "content," + categories;
            }
            return "content,metadata";
        }
    }

    @Override
    public ArchiveFilesExporter readDocuments(Consumer<ReadArchiveDocumentOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ArchiveFilesExporter writeFiles(Consumer<WriteFilesOptions<? extends WriteFilesOptions>> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public ArchiveFilesExporter writeFiles(String path) {
        writeParams.path(path);
        return this;
    }
}
