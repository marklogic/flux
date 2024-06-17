package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read ORC files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-orc.html">Spark's ORC support</a>,
 * with each row being written as a JSON or XML document in MarkLogic.
 */
public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    OrcFilesImporter from(String... paths);

    OrcFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
