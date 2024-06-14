package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read documents and their metadata from MarkLogic and write them to ZIP files on a local filesystem, HDFS, or S3.
 */
public interface ArchiveFilesExporter extends Executor<ArchiveFilesExporter> {

    interface ReadArchiveDocumentOptions extends ReadDocumentsOptions<ReadArchiveDocumentOptions> {
        ReadArchiveDocumentOptions categories(String... categories);
    }

    ArchiveFilesExporter from(Consumer<ReadArchiveDocumentOptions> consumer);

    ArchiveFilesExporter to(Consumer<WriteFilesOptions<? extends WriteFilesOptions>> consumer);

    ArchiveFilesExporter to(String path);
}
