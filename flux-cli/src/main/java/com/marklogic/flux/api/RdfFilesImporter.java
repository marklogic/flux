package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface RdfFilesImporter extends Executor<RdfFilesImporter> {

    interface ReadRdfFilesOptions extends ReadFilesOptions<ReadRdfFilesOptions> {
        ReadRdfFilesOptions compressionType(CompressionType compressionType);
        ReadRdfFilesOptions partitions(Integer partitions);
    }

    interface WriteTriplesDocumentsOptions extends WriteDocumentsOptions<WriteTriplesDocumentsOptions> {
        WriteTriplesDocumentsOptions graph(String graph);

        WriteTriplesDocumentsOptions graphOverride(String graphOverride);
    }

    RdfFilesImporter from(Consumer<ReadRdfFilesOptions> consumer);

    RdfFilesImporter from(String... paths);

    RdfFilesImporter to(Consumer<WriteTriplesDocumentsOptions> consumer);
}
