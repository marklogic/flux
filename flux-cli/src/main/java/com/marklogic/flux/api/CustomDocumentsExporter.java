/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
