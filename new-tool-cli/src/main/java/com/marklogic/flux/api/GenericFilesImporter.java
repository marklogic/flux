package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface GenericFilesImporter extends Executor<GenericFilesImporter> {

    enum DocumentType {
        JSON, TEXT, XML
    }

    interface ReadGenericFilesOptions extends ReadFilesOptions<ReadGenericFilesOptions> {
        ReadGenericFilesOptions compressionType(CompressionType compressionType);
        ReadGenericFilesOptions partitions(Integer partitions);
    }

    interface WriteGenericDocumentsOptions extends WriteDocumentsOptions<WriteGenericDocumentsOptions> {
        WriteGenericDocumentsOptions documentType(DocumentType documentType);
    }

    GenericFilesImporter readFiles(Consumer<ReadGenericFilesOptions> consumer);

    GenericFilesImporter readFiles(String... paths);

    GenericFilesImporter writeDocuments(Consumer<WriteGenericDocumentsOptions> consumer);
}
