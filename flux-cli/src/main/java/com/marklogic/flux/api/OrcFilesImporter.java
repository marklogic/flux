/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read ORC files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-orc.html">Spark's ORC support</a>,
 * and write JSON or XML documents to MarkLogic.
 */
public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    OrcFilesImporter from(String... paths);

    OrcFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
