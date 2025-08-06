/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read documents from MarkLogic and write them using a custom Spark connector or data source.
 */
public interface CustomDocumentsExporter extends Executor<CustomDocumentsExporter> {

    <T extends ReadDocumentsOptions<T>> CustomDocumentsExporter from(Consumer<T> consumer);

    CustomDocumentsExporter to(Consumer<CustomExportWriteOptions> consumer);
}
