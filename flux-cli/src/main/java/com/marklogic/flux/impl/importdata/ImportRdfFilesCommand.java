/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
    abbreviateSynopsis = true,
    description = "Read RDF data from local, HDFS, and S3 files and write the data as managed triples documents in MarkLogic."
)
public class ImportRdfFilesCommand extends AbstractImportFilesCommand<RdfFilesImporter> implements RdfFilesImporter {

    @CommandLine.ArgGroup(exclusive = false, heading = READER_OPTIONS_HEADING, multiplicity = "1")
    private ReadRdfFilesParams readParams = new ReadRdfFilesParams();

    @CommandLine.ArgGroup(exclusive = false, heading = WRITER_OPTIONS_HEADING)
    private WriteTriplesDocumentsParams writeParams = new WriteTriplesDocumentsParams();

    @Override
    protected String getReadFormat() {
        return AbstractCommand.MARKLOGIC_CONNECTOR;
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected Supplier<Map<String, String>> getWriteParams() {
        return writeParams;
    }

    public static class ReadRdfFilesParams extends ReadFilesParams<ReadRdfFilesOptions> implements ReadRdfFilesOptions {

        @CommandLine.Option(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
        private CompressionType compressionType;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private Integer partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "rdf",
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null,
                Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null
            );
        }

        @Override
        public ReadRdfFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Override
        public ReadRdfFilesOptions partitions(Integer partitions) {
            this.partitions = partitions;
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
