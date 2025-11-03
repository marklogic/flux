/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read Avro files from supported file locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-avro.html">Spark's Avro support</a>,
 * and write JSON or XML documents to MarkLogic.
 */
public interface AvroFilesImporter extends Executor<AvroFilesImporter> {

    AvroFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    AvroFilesImporter from(String... paths);

    AvroFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
