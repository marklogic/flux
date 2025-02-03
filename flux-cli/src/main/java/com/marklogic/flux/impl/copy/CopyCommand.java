/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.copy;

import com.marklogic.flux.api.*;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.ConnectionParamsValidator;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.export.ReadDocumentParams;
import com.marklogic.flux.impl.importdata.ClassifierParams;
import com.marklogic.flux.impl.importdata.EmbedderParams;
import com.marklogic.flux.impl.importdata.SplitterParams;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;
import org.apache.spark.sql.SaveMode;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Not able to reuse {@code WriteDocumentParams} as we need alternate annotations for each of the fields so that
 * each "write" param can be prefixed with "--output". This therefore duplicates all the "write" params, as well as
 * all the connection params for writing as well. It relies on unit tests to ensure that the counts of these params
 * are the same to avoid a situation where e.g. a new param is added to {@code WriteDocumentParams} but not added here.
 */
@CommandLine.Command(
    name = "copy",
    description = "Copy documents from one database to another database, which can also be the originating database."
)
public class CopyCommand extends AbstractCommand<DocumentCopier> implements DocumentCopier {

    public static class CopyReadDocumentsParams extends ReadDocumentParams<CopyReadDocumentsOptions> implements DocumentCopier.CopyReadDocumentsOptions {

        @CommandLine.Option(names = "--categories", description = "Comma-delimited sequence of categories of data to include. " +
            "Valid choices are: content, metadata (for all types of metadata), collections, permissions, quality, properties, and metadatavalues.")
        private String categories = "content,metadata";

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_DOCUMENTS_CATEGORIES, categories
            );
        }

        @Override
        public DocumentCopier.CopyReadDocumentsOptions categories(String... categories) {
            this.categories = Stream.of(categories).collect(Collectors.joining(","));
            return this;
        }
    }

    public static class CopyWriteDocumentsParams implements CopyWriteDocumentsOptions {

        @CommandLine.Option(
            names = "--output-abort-on-write-failure",
            description = "Causes the command to fail when a batch of documents cannot be written to MarkLogic."
        )
        private boolean abortOnWriteFailure;

        @CommandLine.Option(
            names = "--output-batch-size",
            description = "The maximum number of documents written in a single call to MarkLogic."
        )
        private int batchSize = 100;

        @CommandLine.Option(
            names = "--output-collections",
            description = "Comma-delimited sequence of collection names to add to each document."
        )
        private String collections;

        @CommandLine.Option(
            names = "--output-failed-documents-path",
            description = "File path for writing an archive file containing failed documents and their metadata. " +
                "The documents in the archive file can then be retried via the 'import-archive-files' command."
        )
        private String failedDocumentsPath;

        @CommandLine.Option(
            names = "--output-log-progress",
            description = "Log a count of total documents written every time this many documents are written."
        )
        private int logProgress = 10000;

        @CommandLine.Option(
            names = "--output-permissions",
            description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to each document - e.g. role1,read,role2,update,role3,execute."
        )
        private String permissions;

        @CommandLine.Option(
            names = "--output-temporal-collection",
            description = "Name of a temporal collection to assign to each document."
        )
        private String temporalCollection;

        @CommandLine.Option(
            names = "--output-thread-count",
            description = "The total number of threads used across all partitions when writing batches of documents to MarkLogic."
        )
        private int threadCount = 4;

        @CommandLine.Option(
            names = "--output-thread-count-per-partition",
            description = "The number of threads used by each partition worker when writing batches of documents to MarkLogic. " +
                "Takes precedence over the '--output-thread-count' option."
        )
        private int threadCountPerPartition;

        @CommandLine.Option(
            names = "--output-transform",
            description = "Name of a MarkLogic REST API transform to apply to each document."
        )
        private String transform;

        @CommandLine.Option(
            names = "--output-transform-params",
            description = "Comma-delimited sequence of REST API transform parameter names and values - e.g. param1,value1,param2,value2."
        )
        private String transformParams;

        @CommandLine.Option(
            names = "--output-transform-params-delimiter",
            description = "Delimiter to use instead of a comma for the '--output-transform-params' parameter."
        )
        private String transformParamsDelimiter;

        @CommandLine.Option(
            names = "--output-uri-prefix",
            description = "String to prepend to each document URI."
        )
        private String uriPrefix;

        @CommandLine.Option(
            names = "--output-uri-replace",
            description = "Comma-delimited sequence of regular expressions and replacement strings for modifying " +
                "the URI of each document - e.g. regex,'value',regex,'value'. " +
                "Each replacement string must be enclosed by single quotes."
        )
        private String uriReplace;

        @CommandLine.Option(
            names = "--output-uri-suffix",
            description = "String to append to each document URI."
        )
        private String uriSuffix;

        @CommandLine.Option(
            names = "--output-uri-template",
            description = "Defines a template for constructing each document URI. " +
                "See the Flux user guide on controlling document URIs for more information."
        )
        private String uriTemplate;

        @CommandLine.Mixin
        private SplitterParams splitterParams = new SplitterParams();

        @CommandLine.Mixin
        private EmbedderParams embedderParams = new EmbedderParams();

        @CommandLine.Mixin
        private ClassifierParams classifierParams = new ClassifierParams();

        protected Map<String, String> makeOptions() {
            Map<String, String> options = splitterParams.makeOptions();
            options.putAll(embedderParams.makeOptions());
            return OptionsUtil.addOptions(options,
                Options.WRITE_ABORT_ON_FAILURE, abortOnWriteFailure ? "true" : null,
                Options.WRITE_ARCHIVE_PATH_FOR_FAILED_DOCUMENTS, failedDocumentsPath,
                Options.WRITE_BATCH_SIZE, OptionsUtil.intOption(batchSize),
                Options.WRITE_COLLECTIONS, collections,
                Options.WRITE_LOG_PROGRESS, OptionsUtil.intOption(logProgress),
                Options.WRITE_PERMISSIONS, permissions,
                Options.WRITE_TEMPORAL_COLLECTION, temporalCollection,
                Options.WRITE_THREAD_COUNT, OptionsUtil.intOption(threadCount),
                Options.WRITE_THREAD_COUNT_PER_PARTITION, OptionsUtil.intOption(threadCountPerPartition),
                Options.WRITE_TRANSFORM_NAME, transform,
                Options.WRITE_TRANSFORM_PARAMS, transformParams,
                Options.WRITE_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
                Options.WRITE_URI_PREFIX, uriPrefix,
                Options.WRITE_URI_REPLACE, uriReplace,
                Options.WRITE_URI_SUFFIX, uriSuffix,
                Options.WRITE_URI_TEMPLATE, uriTemplate
            );
        }

        @Override
        public CopyWriteDocumentsParams abortOnWriteFailure(boolean value) {
            this.abortOnWriteFailure = value;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams collections(String... collections) {
            this.collections = Stream.of(collections).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public CopyWriteDocumentsParams collectionsString(String commaDelimitedCollections) {
            this.collections = commaDelimitedCollections;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams failedDocumentsPath(String path) {
            this.failedDocumentsPath = path;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams logProgress(int interval) {
            this.logProgress = interval;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams permissionsString(String rolesAndCapabilities) {
            this.permissions = rolesAndCapabilities;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams splitter(Consumer<SplitterOptions> consumer) {
            consumer.accept(splitterParams);
            return this;
        }

        @Override
        public CopyWriteDocumentsOptions embedder(Consumer<EmbedderOptions> consumer) {
            consumer.accept(embedderParams);
            return this;
        }

        @Override
        public CopyWriteDocumentsOptions classifier(Consumer<ClassifierOptions> consumer) {
            consumer.accept(classifierParams);
            return this;
        }

        @Override
        public CopyWriteDocumentsParams temporalCollection(String temporalCollection) {
            this.temporalCollection = temporalCollection;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams threadCountPerPartition(int threadCountPerPartition) {
            this.threadCountPerPartition = threadCountPerPartition;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams transform(String transform) {
            this.transform = transform;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams transformParams(String delimitedNamesAndValues) {
            this.transformParams = delimitedNamesAndValues;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams transformParamsDelimiter(String delimiter) {
            this.transformParamsDelimiter = delimiter;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams uriPrefix(String uriPrefix) {
            this.uriPrefix = uriPrefix;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams uriReplace(String uriReplace) {
            this.uriReplace = uriReplace;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams uriSuffix(String uriSuffix) {
            this.uriSuffix = uriSuffix;
            return this;
        }

        @Override
        public CopyWriteDocumentsParams uriTemplate(String uriTemplate) {
            this.uriTemplate = uriTemplate;
            return this;
        }
    }

    @CommandLine.Mixin
    protected final CopyReadDocumentsParams readParams = new CopyReadDocumentsParams();

    @CommandLine.Mixin
    protected final CopyWriteDocumentsParams writeParams = new CopyWriteDocumentsParams();

    @CommandLine.ArgGroup(exclusive = false, heading = "\nOutput Connection Options\n", order = 2)
    private OutputConnectionParams outputConnectionParams = new OutputConnectionParams();

    @Override
    public void execute() {
        outputConnectionParams.validateConnectionString("output connection string");
        super.execute();
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        super.validateCommandLineOptions(parseResult);
        if (outputConnectionParams.atLeastOutputConnectionParameterExists(parseResult)) {
            CopyCommand copyCommand = (CopyCommand) parseResult.subcommand().commandSpec().userObject();
            new ConnectionParamsValidator(true).validate(copyCommand.outputConnectionParams);
        }
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeOutputConnectionOptions())
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    protected Map<String, String> makeOutputConnectionOptions() {
        Map<String, String> options = outputConnectionParams.makeOptions();
        // If user doesn't specify any "--output" connection options, then reuse the connection for reading data.
        return options.isEmpty() ? getConnectionParams().makeOptions() : options;
    }

    @Override
    public DocumentCopier from(Consumer<CopyReadDocumentsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public DocumentCopier to(Consumer<CopyWriteDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public DocumentCopier outputConnection(Consumer<ConnectionOptions> consumer) {
        consumer.accept(outputConnectionParams);
        return this;
    }

    @Override
    public DocumentCopier outputConnectionString(String connectionString) {
        outputConnectionParams.connectionString(connectionString);
        return this;
    }
}
