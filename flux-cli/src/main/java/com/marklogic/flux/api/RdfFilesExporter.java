package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface RdfFilesExporter extends Executor<RdfFilesExporter> {

    interface ReadTriplesDocumentsOptions {

        ReadTriplesDocumentsOptions graphs(String... graphs);

        ReadTriplesDocumentsOptions stringQuery(String stringQuery);

        ReadTriplesDocumentsOptions uris(String... uris);

        ReadTriplesDocumentsOptions query(String query);

        ReadTriplesDocumentsOptions options(String options);

        ReadTriplesDocumentsOptions collections(String... collections);

        ReadTriplesDocumentsOptions directory(String directory);

        ReadTriplesDocumentsOptions batchSize(Integer batchSize);

        ReadTriplesDocumentsOptions partitionsPerForest(Integer partitionsPerForest);
    }

    interface WriteRdfFilesOptions extends WriteFilesOptions<WriteRdfFilesOptions> {
        WriteRdfFilesOptions format(String format);

        WriteRdfFilesOptions graphOverride(String graphOverride);

        WriteRdfFilesOptions gzip();
    }

    RdfFilesExporter from(Consumer<ReadTriplesDocumentsOptions> consumer);

    RdfFilesExporter to(Consumer<WriteRdfFilesOptions> consumer);

    RdfFilesExporter to(String path);
}
