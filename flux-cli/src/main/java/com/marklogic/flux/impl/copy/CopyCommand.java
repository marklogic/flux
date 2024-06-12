package com.marklogic.flux.impl.copy;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.export.ReadDocumentParams;
import com.marklogic.flux.api.ConnectionOptions;
import com.marklogic.flux.api.DocumentCopier;
import com.marklogic.flux.api.WriteDocumentsOptions;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

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
@Parameters(commandDescription = "Copy documents from one to another database, which includes the originating database.")
public class CopyCommand extends AbstractCommand<DocumentCopier> implements DocumentCopier {

    public static class CopyReadDocumentsParams extends ReadDocumentParams<CopyReadDocumentsOptions> implements DocumentCopier.CopyReadDocumentsOptions {

        @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of data to include. " +
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

    public static class CopyWriteDocumentsParams implements WriteDocumentsOptions<CopyWriteDocumentsParams> {

        @Parameter(
            names = "--output-abort-on-write-failure",
            description = "Include this option to cause the command to fail when a batch of documents cannot be written to MarkLogic."
        )
        private Boolean abortOnWriteFailure;

        @Parameter(
            names = "--output-batch-size",
            description = "The number of documents written in a call to MarkLogic."
        )
        private Integer batchSize = 100;

        @Parameter(
            names = "--output-collections",
            description = "Comma-delimited string of collection names to add to each document."
        )
        private String collections;

        @Parameter(
            names = "--output-failed-documents-path",
            description = "File path for writing an archive file containing failed documents and their metadata."
        )
        private String failedDocumentsPath;

        @Parameter(
            names = "--output-permissions",
            description = "Comma-delimited string of role names and capabilities to add to each document - e.g. role1,read,role2,update,role3,execute."
        )
        private String permissions;

        @Parameter(
            names = "--output-temporal-collection",
            description = "Name of a temporal collection to assign to each document."
        )
        private String temporalCollection;

        @Parameter(
            names = "--output-thread-count",
            description = "The number of threads used by each partition worker when writing batches of documents to MarkLogic."
        )
        private Integer threadCount = 4;

        @Parameter(
            names = "--output-total-thread-count",
            description = "The total number of threads used across all partitions when writing batches of documents to MarkLogic."
        )
        private Integer totalThreadCount;

        @Parameter(
            names = "--output-transform",
            description = "Name of a MarkLogic REST API transform to apply to each document."
        )
        private String transform;

        @Parameter(
            names = "--output-transform-params",
            description = "Comma-delimited string of REST API transform parameter names and values - e.g. param1,value1,param2,value2."
        )
        private String transformParams;

        @Parameter(
            names = "--output-transform-params-delimiter",
            description = "Delimiter to use instead of a comma for the '--transform-params' parameter."
        )
        private String transformParamsDelimiter;

        @Parameter(
            names = "--output-uri-prefix",
            description = "String to prepend to each document URI."
        )
        private String uriPrefix;

        @Parameter(
            names = "--output-uri-replace",
            description = "Modify the URI for a document via a comma-delimited list of regular expression " +
                "and replacement string pairs - e.g. regex,'value',regex,'value'. Each replacement string must be enclosed by single quotes."
        )
        private String uriReplace;

        @Parameter(
            names = "--output-uri-suffix",
            description = "String to append to each document URI."
        )
        private String uriSuffix;

        @Parameter(
            names = "--output-uri-template",
            description = "String defining a template for constructing each document URI. " +
                "See https://marklogic.github.io/marklogic-spark-connector/writing.html for more information."
        )
        private String uriTemplate;

        protected Map<String, String> makeOptions() {
            return OptionsUtil.makeOptions(
                Options.WRITE_ABORT_ON_FAILURE, abortOnWriteFailure != null ? Boolean.toString(abortOnWriteFailure) : "false",
                Options.WRITE_ARCHIVE_PATH_FOR_FAILED_DOCUMENTS, failedDocumentsPath,
                Options.WRITE_BATCH_SIZE, batchSize != null ? batchSize.toString() : null,
                Options.WRITE_COLLECTIONS, collections,
                Options.WRITE_PERMISSIONS, permissions,
                Options.WRITE_TEMPORAL_COLLECTION, temporalCollection,
                Options.WRITE_THREAD_COUNT, threadCount != null ? threadCount.toString() : null,
                Options.WRITE_TOTAL_THREAD_COUNT, totalThreadCount != null ? totalThreadCount.toString() : null,
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
        public CopyWriteDocumentsParams abortOnWriteFailure(Boolean value) {
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
        public CopyWriteDocumentsParams permissionsString(String rolesAndCapabilities) {
            this.permissions = rolesAndCapabilities;
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
        public CopyWriteDocumentsParams totalThreadCount(int totalThreadCount) {
            this.totalThreadCount = totalThreadCount;
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

    @ParametersDelegate
    private CopyReadDocumentsParams readParams = new CopyReadDocumentsParams();

    @ParametersDelegate
    private OutputConnectionParams outputConnectionParams = new OutputConnectionParams();

    @ParametersDelegate
    protected final CopyWriteDocumentsParams writeParams = new CopyWriteDocumentsParams();

    @Override
    public void execute() {
        outputConnectionParams.validateConnectionString("output connection string");
        super.execute();
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
    public DocumentCopier readDocuments(Consumer<CopyReadDocumentsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public DocumentCopier writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer) {
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
