/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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

        /**
         * Force a document type for any document with an unrecognized URI extension.
         */
        WriteArchiveDocumentsOptions documentType(DocumentType documentType);

        /**
         * @param commaDelimitedExtensions A comma-delimited list of file extensions intended to be used when streaming
         *                                 entries from an archive and also using a REST transform to convert documents
         *                                 with format=BINARY and one of the given file extensions into a binary. A
         *                                 REST transform is required to convert a document to a binary when its URI
         *                                 extension matches a MarkLogic mimetype that is not binary.
         * @since 2.1.0
         */
        WriteArchiveDocumentsOptions streamingTransformBinaryWithExtension(String commaDelimitedExtensions);
    }

    ArchiveFilesImporter from(Consumer<ReadArchiveFilesOptions> consumer);

    ArchiveFilesImporter from(String... paths);

    /**
     * @since 1.1.0
     */
    ArchiveFilesImporter streaming();

    /**
     * Added in the 1.4.0 release to support additional options. In Flux 2.0.0, this will be renamed to
     * "to" and the deprecated "to" method will be removed.
     *
     * @since 1.4.0
     */
    ArchiveFilesImporter toOptions(Consumer<WriteArchiveDocumentsOptions> consumer);
}
