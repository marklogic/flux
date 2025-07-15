/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 Flux archive files and write the documents in each archive to MarkLogic.
 */
public interface ArchiveFilesImporter extends Executor<ArchiveFilesImporter> {

    interface ReadArchiveFilesOptions extends ReadFilesOptions<ReadArchiveFilesOptions> {
        ReadArchiveFilesOptions categories(String... categories);

        ReadArchiveFilesOptions partitions(int partitions);

        ReadArchiveFilesOptions encoding(String encoding);
    }

    /**
     * @since 1.4.0
     */
    interface WriteArchiveDocumentsOptions extends WriteDocumentsOptions<WriteArchiveDocumentsOptions> {
        WriteArchiveDocumentsOptions documentType(DocumentType documentType);
    }

    ArchiveFilesImporter from(Consumer<ReadArchiveFilesOptions> consumer);

    ArchiveFilesImporter from(String... paths);

    /**
     * @since 1.1.0
     */
    ArchiveFilesImporter streaming();

    /**
     * @deprecated Use {@link #toOptions(Consumer)} instead
     */
    @SuppressWarnings("java:S1133") // Telling Sonar we don't need a reminder to remove this some day.
    @Deprecated(since = "1.4.0", forRemoval = true)
    ArchiveFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);

    /**
     * Added in the 1.4.0 release to support additional options. In Flux 2.0.0, this will be renamed to
     * "to" and the deprecated "to" method will be removed.
     *
     * @since 1.4.0
     */
    ArchiveFilesImporter toOptions(Consumer<WriteArchiveDocumentsOptions> consumer);
}
