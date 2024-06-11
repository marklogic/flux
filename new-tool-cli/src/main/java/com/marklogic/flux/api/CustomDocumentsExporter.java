package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface CustomDocumentsExporter extends Executor<CustomDocumentsExporter> {

    CustomDocumentsExporter readDocuments(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer);

    CustomDocumentsExporter writeDocuments(Consumer<CustomExportWriteOptions> consumer);
}
