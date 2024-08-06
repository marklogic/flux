/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read documents from MarkLogic and write them using a custom Spark connector or data source.
 */
public interface CustomDocumentsExporter extends Executor<CustomDocumentsExporter> {

    CustomDocumentsExporter from(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer);

    CustomDocumentsExporter to(Consumer<CustomExportWriteOptions> consumer);
}
