/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic.
 */
public interface GenericFilesImporter extends Executor<GenericFilesImporter> {

    enum DocumentType {
        JSON, TEXT, XML
    }

    interface ReadGenericFilesOptions extends ReadFilesOptions<ReadGenericFilesOptions> {
        ReadGenericFilesOptions compressionType(CompressionType compressionType);

        ReadGenericFilesOptions partitions(int partitions);

        ReadGenericFilesOptions encoding(String encoding);
    }

    interface WriteGenericDocumentsOptions extends WriteDocumentsOptions<WriteGenericDocumentsOptions> {
        WriteGenericDocumentsOptions documentType(DocumentType documentType);

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractText();

        /**
         * @since 1.3.0
         */
        WriteGenericDocumentsOptions extractedTextDocumentType(DocumentType documentType);

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
