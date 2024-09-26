/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read documents and their metadata from MarkLogic and write them to ZIP files on a local filesystem, HDFS, or S3.
 */
public interface ArchiveFilesExporter extends Executor<ArchiveFilesExporter> {

    interface ReadArchiveDocumentOptions extends ReadDocumentsOptions<ReadArchiveDocumentOptions> {
        ReadArchiveDocumentOptions categories(String... categories);
    }

    interface WriteArchiveFilesOptions extends WriteFilesOptions<WriteArchiveFilesOptions> {
        WriteArchiveFilesOptions encoding(String encoding);
    }

    ArchiveFilesExporter from(Consumer<ReadArchiveDocumentOptions> consumer);

    /**
     * @since 1.1.0
     */
    ArchiveFilesExporter streaming();

    ArchiveFilesExporter to(Consumer<WriteArchiveFilesOptions> consumer);

    ArchiveFilesExporter to(String path);
}
