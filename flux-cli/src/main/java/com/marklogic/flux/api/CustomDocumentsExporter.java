package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface CustomDocumentsExporter extends Executor<CustomDocumentsExporter> {

    CustomDocumentsExporter from(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer);

    CustomDocumentsExporter to(Consumer<CustomExportWriteOptions> consumer);
}
