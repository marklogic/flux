/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read RDF data from local, HDFS, and S3 files and write the data as managed triples documents in MarkLogic.
 */
public interface RdfFilesImporter extends Executor<RdfFilesImporter> {

    interface ReadRdfFilesOptions extends ReadFilesOptions<ReadRdfFilesOptions> {
        ReadRdfFilesOptions compressionType(CompressionType compressionType);
        ReadRdfFilesOptions partitions(int partitions);
    }

    interface WriteTriplesDocumentsOptions extends WriteDocumentsOptions<WriteTriplesDocumentsOptions> {
        WriteTriplesDocumentsOptions graph(String graph);

        WriteTriplesDocumentsOptions graphOverride(String graphOverride);
    }

    RdfFilesImporter from(Consumer<ReadRdfFilesOptions> consumer);

    RdfFilesImporter from(String... paths);

    RdfFilesImporter to(Consumer<WriteTriplesDocumentsOptions> consumer);
}
