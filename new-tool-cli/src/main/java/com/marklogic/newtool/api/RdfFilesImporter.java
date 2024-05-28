package com.marklogic.newtool.api;

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

    RdfFilesImporter readFiles(Consumer<ReadRdfFilesOptions> consumer);

    RdfFilesImporter readFiles(String... paths);

    RdfFilesImporter writeDocuments(Consumer<WriteTriplesDocumentsOptions> consumer);
}
