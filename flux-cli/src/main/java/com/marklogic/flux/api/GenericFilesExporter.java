package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface GenericFilesExporter extends Executor<GenericFilesExporter> {

    interface WriteGenericFilesOptions {
        WriteGenericFilesOptions path(String path);

        WriteGenericFilesOptions compressionType(CompressionType compressionType);

        WriteGenericFilesOptions prettyPrint(Boolean value);

        WriteGenericFilesOptions zipFileCount(Integer zipFileCount);

        WriteGenericFilesOptions s3AddCredentials();

        WriteGenericFilesOptions s3Endpoint(String endpoint);
    }

    GenericFilesExporter readDocuments(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer);

    GenericFilesExporter writeFiles(Consumer<WriteGenericFilesOptions> consumer);

    GenericFilesExporter writeFiles(String path);
}
