/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.CompressionType;
import com.marklogic.flux.api.RdfFilesImporter;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@CommandLine.Command(
    name = "import-rdf-files",
    description = "Read RDF data from supported file locations and write the data as managed triples documents in MarkLogic."
)
public class ImportRdfFilesCommand extends AbstractImportFilesCommand<RdfFilesImporter> implements RdfFilesImporter {

    @CommandLine.Mixin
    private ReadRdfFilesParams readParams = new ReadRdfFilesParams();

    @CommandLine.Mixin
    private WriteTriplesDocumentsParams writeParams = new WriteTriplesDocumentsParams();

    @Override
    protected String getReadFormat() {
        return AbstractCommand.MARKLOGIC_CONNECTOR;
    }

    @Override
    protected IReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected Supplier<Map<String, String>> getWriteParams() {
        return writeParams;
    }

    public static class ReadRdfFilesParams extends ReadFilesParams<ReadRdfFilesOptions> implements ReadRdfFilesOptions {

        @CommandLine.Option(names = "--compression", description = "When importing compressed files, specify the type of compression used. "
            + OptionsUtil.VALID_VALUES_DESCRIPTION)
        private CompressionType compressionType;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private int partitions;

        @CommandLine.Option(
            names = "--zip-max-entry-bytes",
            description = "Maximum number of uncompressed bytes to read from a single zip entry. " +
                "Set to a positive integer to enable protection. Any value less than 1 (including 0) disables the limit."
        )
        private Long zipMaxEntryBytes;

        @CommandLine.Option(
            names = "--zip-max-entry-count",
            description = "Maximum number of entries to process from a single zip archive. " +
                "Set to a positive integer to enable protection. Any value less than 1 (including 0) disables the limit."
        )
        private Integer zipMaxEntryCount;

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "rdf",
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null,
                Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions)
            );
            if (zipMaxEntryBytes != null && zipMaxEntryBytes > 0) {
                options.put(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES, String.valueOf(zipMaxEntryBytes));
            }
            if (zipMaxEntryCount != null && zipMaxEntryCount > 0) {
                options.put(Options.READ_ZIP_MAX_ENTRY_COUNT, String.valueOf(zipMaxEntryCount));
            }
            return options;
        }

        @Override
        public ReadRdfFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Override
        public ReadRdfFilesOptions partitions(int partitions) {
            this.partitions = partitions;
            return this;
        }

        @Override
        public ReadRdfFilesOptions zipMaxUncompressedEntryBytes(long bytes) {
            this.zipMaxEntryBytes = bytes;
            return this;
        }

        @Override
        public ReadRdfFilesOptions zipMaxEntryCount(int count) {
            this.zipMaxEntryCount = count;
            return this;
        }
    }

    public static class WriteTriplesDocumentsParams extends WriteDocumentParams<WriteTriplesDocumentsOptions> implements WriteTriplesDocumentsOptions {

        @CommandLine.Option(names = "--graph", description = "Specify the graph URI for each triple not already associated with a graph. If not set, " +
            "triples will be added to the default MarkLogic graph - http://marklogic.com/semantics#default-graph . ")
        private String graph;

        @CommandLine.Option(names = "--graph-override", description = "Specify the graph URI for each triple to be included in, " +
            "even if is already associated with a graph.")
        private String graphOverride;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_GRAPH, graph,
                Options.WRITE_GRAPH_OVERRIDE, graphOverride
            );
        }

        @Override
        public WriteTriplesDocumentsOptions graph(String graph) {
            this.graph = graph;
            return this;
        }

        @Override
        public WriteTriplesDocumentsOptions graphOverride(String graphOverride) {
            this.graphOverride = graphOverride;
            return this;
        }
    }

    @Override
    public RdfFilesImporter from(Consumer<ReadRdfFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public RdfFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public RdfFilesImporter to(Consumer<WriteTriplesDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
