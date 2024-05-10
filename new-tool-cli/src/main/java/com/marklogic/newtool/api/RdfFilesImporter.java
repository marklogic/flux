package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface RdfFilesImporter extends Executor<RdfFilesImporter> {

    interface ReadRdfFilesOptions extends ReadFilesOptions<ReadRdfFilesOptions> {
        ReadRdfFilesOptions compressionType(CompressionType compressionType);
    }

    interface WriteTriplesDocumentsOptions extends WriteDocumentsOptions<WriteTriplesDocumentsOptions> {
        WriteTriplesDocumentsOptions graph(String graph);

        WriteTriplesDocumentsOptions graphOverride(String graphOverride);
    }

    RdfFilesImporter readFiles(Consumer<ReadRdfFilesOptions> consumer);

    RdfFilesImporter writeDocuments(Consumer<WriteTriplesDocumentsOptions> consumer);
}
