/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read delimited text files from local, HDFS, and S3 locations using
 * <a href="https://spark.apache.org/docs/latest/sql-data-sources-csv.html">Spark's CSV support</a>,
 * and write JSON or XML documents to MarkLogic.
 */
public interface DelimitedFilesImporter extends Executor<DelimitedFilesImporter> {

    DelimitedFilesImporter from(Consumer<ReadDelimitedFilesOptions> consumer);

    DelimitedFilesImporter from(String... paths);

    DelimitedFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);

    interface ReadDelimitedFilesOptions extends ReadFilesOptions<ReadDelimitedFilesOptions> {
        ReadDelimitedFilesOptions delimiter(String delimiter);

        ReadDelimitedFilesOptions additionalOptions(Map<String, String> options);

        ReadDelimitedFilesOptions groupBy(String columnName);

        ReadDelimitedFilesOptions aggregateColumns(String newColumnName, String... columns);

        ReadDelimitedFilesOptions encoding(String encoding);

        ReadDelimitedFilesOptions uriIncludeFilePath(boolean value);
    }
}
