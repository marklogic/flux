package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read Avro files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-avro.html">Spark's Avro support</a>,
 * with each row being written as a JSON or XML document in MarkLogic.
 */
public interface AvroFilesImporter extends Executor<AvroFilesImporter> {

    AvroFilesImporter from(Consumer<ReadSparkFilesOptions> consumer);

    AvroFilesImporter from(String... paths);

    AvroFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}
