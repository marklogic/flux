package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 Flux archive files and write the documents in each archive to MarkLogic.
 */
public interface ArchiveFilesImporter extends Executor<ArchiveFilesImporter> {

    interface ReadArchiveFilesOptions extends ReadFilesOptions<ReadArchiveFilesOptions> {
        ReadArchiveFilesOptions categories(String... categories);

        ReadArchiveFilesOptions partitions(Integer partitions);
    }

    ArchiveFilesImporter from(Consumer<ReadArchiveFilesOptions> consumer);

    ArchiveFilesImporter from(String... paths);

    ArchiveFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}