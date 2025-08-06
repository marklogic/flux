/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read ORC files from supported file locations using
 * <a href="https://spark.apache.org/docs/3.5.6/sql-data-sources-orc.html">Spark's ORC support</a>,
 * and write JSON or XML documents to MarkLogic.
 */
public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    OrcFilesImporter from(String... paths);

    OrcFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
