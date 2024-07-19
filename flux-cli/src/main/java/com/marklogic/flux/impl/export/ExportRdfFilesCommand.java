/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.RdfFilesExporter;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "export-rdf-files",
    description = "Read triples from MarkLogic and write them to a local filesystem, HDFS, or S3."
)
public class ExportRdfFilesCommand extends AbstractCommand<RdfFilesExporter> implements RdfFilesExporter {

    @CommandLine.Mixin
    protected ReadTriplesParams readParams = new ReadTriplesParams();

    @CommandLine.Mixin
    protected WriteRdfFilesParams writeParams = new WriteRdfFilesParams();

    @Override
    protected void validateDuringApiUsage() {
        if (readParams.getQueryOptions().isEmpty()) {
            throw new FluxException("Must specify at least one of the following for the triples to export: " +
                "collections; a directory; graphs; a string query; a structured, serialized, or combined query; or URIs.");
        }
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        super.validateCommandLineOptions(parseResult);
        OptionsUtil.verifyHasAtLeastOneOption(parseResult,
            "--collections", "--directory", "--graphs", "--query", "--string-query", "--uris");
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        final int fileCount = writeParams.getFileCount();
        if (fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.get())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writeParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(MARKLOGIC_CONNECTOR)
            .options(writeParams.get())
            .mode(SaveMode.Append)
            .save(writeParams.getPath());
    }

    public static class ReadTriplesParams implements Supplier<Map<String, String>>, RdfFilesExporter.ReadTriplesDocumentsOptions {

        @CommandLine.Option(names = "--uris", description = "Newline-delimited sequence of document URIs to retrieve. Can be combined " +
            "with --collections, --directory, and --string-query. If specified, --query will be ignored.")
        private String uris;

        @CommandLine.Option(names = "--string-query", description = "A query utilizing the MarkLogic search grammar; " +
            "see %nhttps://docs.marklogic.com/guide/search-dev/string-query for more information.")
        private String stringQuery;

        @CommandLine.Option(names = "--query", description = "A JSON or XML representation of a structured query, serialized CTS query, or combined query. " +
            "See https://docs.marklogic.com/guide/rest-dev/search#id_49329 for more information.")
        private String query;

        @CommandLine.Option(names = "--graphs", description = "Comma-delimited sequence of MarkLogic graph names by which to constrain the query.")
        private String graphs;

        @CommandLine.Option(names = "--collections", description = "Comma-delimited sequence of collection names by which to constrain the query.")
        private String collections;

        @CommandLine.Option(names = "--directory", description = "Database directory by which to constrain the query.")
        private String directory;

        @CommandLine.Option(names = "--options", description = "Name of a set of MarkLogic REST API search options.")
        private String options;

        @CommandLine.Option(names = "--base-iri", description = "Base IRI to prepend to the graph of a triple when the graph is relative and not absolute.")
        private String baseIri;

        @CommandLine.Option(names = "--batch-size", description = "Number of documents to retrieve in each call to MarkLogic.")
        private int batchSize = 100;

        @CommandLine.Option(names = "--partitions-per-forest", description = "Number of partition readers to create for each forest.")
        private int partitionsPerForest = 4;

        public Map<String, String> getQueryOptions() {
            return OptionsUtil.makeOptions(
                Options.READ_TRIPLES_GRAPHS, graphs,
                Options.READ_TRIPLES_COLLECTIONS, collections,
                Options.READ_TRIPLES_QUERY, query,
                Options.READ_TRIPLES_STRING_QUERY, stringQuery,
                Options.READ_TRIPLES_URIS, uris,
                Options.READ_TRIPLES_DIRECTORY, directory
            );
        }

        @Override
        public Map<String, String> get() {
            return OptionsUtil.addOptions(getQueryOptions(),
                Options.READ_TRIPLES_OPTIONS, options,
                Options.READ_TRIPLES_BASE_IRI, baseIri,
                Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST, OptionsUtil.intOption(partitionsPerForest),
                Options.READ_BATCH_SIZE, OptionsUtil.intOption(batchSize)
            );
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions graphs(String... graphs) {
            this.graphs = Stream.of(graphs).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions stringQuery(String stringQuery) {
            this.stringQuery = stringQuery;
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions uris(String... uris) {
            this.uris = Stream.of(uris).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions query(String query) {
            this.query = query;
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions options(String options) {
            this.options = options;
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions collections(String... collections) {
            this.collections = Stream.of(collections).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions directory(String directory) {
            this.directory = directory;
            return this;
        }

        @Override
        public ReadTriplesDocumentsOptions baseIri(String baseIri) {
            this.baseIri = baseIri;
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        @Override
        public RdfFilesExporter.ReadTriplesDocumentsOptions partitionsPerForest(int partitionsPerForest) {
            this.partitionsPerForest = partitionsPerForest;
            return this;
        }
    }

    public static class WriteRdfFilesParams extends WriteFilesParams<WriteRdfFilesOptions> implements WriteRdfFilesOptions {

        @CommandLine.Option(names = "--format", description = "RDF file format; supported values are 'nq', 'nt', 'rdfthrift', 'trig', 'trix', and 'ttl'.")
        private String format = "ttl";

        @CommandLine.Option(names = "--graph-override", description = "Semantic graph to include in each file. Only allowed when '--format' is 'nq' or 'trig'.")
        private String graphOverride;

        @CommandLine.Option(names = "--gzip", description = "GZIP each file.")
        private boolean gzip;

        @Override
        public Map<String, String> get() {
            return OptionsUtil.makeOptions(
                Options.WRITE_RDF_FILES_FORMAT, format,
                Options.WRITE_RDF_FILES_GRAPH, graphOverride,
                Options.WRITE_FILES_COMPRESSION, gzip ? "gzip" : null
            );
        }

        @Override
        public WriteRdfFilesOptions format(String format) {
            this.format = format;
            return this;
        }

        @Override
        public WriteRdfFilesOptions graphOverride(String graphOverride) {
            this.graphOverride = graphOverride;
            return this;
        }

        @Override
        public WriteRdfFilesOptions gzip() {
            this.gzip = true;
            return this;
        }
    }

    @Override
    public RdfFilesExporter from(Consumer<ReadTriplesDocumentsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public RdfFilesExporter to(Consumer<WriteRdfFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public RdfFilesExporter to(String path) {
        writeParams.path(path);
        return this;
    }
}
