/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
    }

    GenericFilesImporter from(Consumer<ReadGenericFilesOptions> consumer);

    GenericFilesImporter from(String... paths);

    GenericFilesImporter to(Consumer<WriteGenericDocumentsOptions> consumer);
}
