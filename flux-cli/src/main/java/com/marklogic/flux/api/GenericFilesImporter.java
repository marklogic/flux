/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic.
 */
public interface GenericFilesImporter extends Executor<GenericFilesImporter> {

    interface ReadGenericFilesOptions extends ReadFilesOptions<ReadGenericFilesOptions> {
        ReadGenericFilesOptions compressionType(CompressionType compressionType);

        ReadGenericFilesOptions partitions(int partitions);

        ReadGenericFilesOptions encoding(String encoding);
    }

    interface WriteGenericDocumentsOptions extends WriteDocumentsOptions<WriteGenericDocumentsOptions> {
        /**
         * @since 1.4.0
         */
        WriteGenericDocumentsOptions documentType(com.marklogic.flux.api.DocumentType documentType);

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractText();

        /**
         * @param documentType can be "json" or "xml"; defaults to "json" if not specified or if an unrecognized value is provided.
         * @since 1.4.0
         */
        WriteGenericDocumentsOptions extractedTextDocumentType(String documentType);

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractedTextCollections(String commaDelimitedCollections);

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractedTextPermissionsString(String rolesAndCapabilities);

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractedTextDropSource();
    }

    GenericFilesImporter from(Consumer<ReadGenericFilesOptions> consumer);

    GenericFilesImporter from(String... paths);

    /**
     * @since 1.1.0
     */
    GenericFilesImporter streaming();

    GenericFilesImporter to(Consumer<WriteGenericDocumentsOptions> consumer);
}
