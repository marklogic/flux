package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read delimited text files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-csv.html">Spark's CSV support</a>,
 * with each row being written as a JSON or XML document to MarkLogic.
 */
public interface DelimitedFilesImporter extends Executor<DelimitedFilesImporter> {

    DelimitedFilesImporter from(Consumer<ReadTabularFilesOptions> consumer);

    DelimitedFilesImporter from(String... paths);

    DelimitedFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);

}
