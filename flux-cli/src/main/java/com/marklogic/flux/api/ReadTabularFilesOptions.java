package com.marklogic.flux.api;

import java.util.Map;

/**
 * For objects that can import tabular files, where the data source supports additional options
 * that do not have dedicated methods in the importer interface.
 */
public interface ReadTabularFilesOptions extends ReadFilesOptions<ReadTabularFilesOptions> {

    ReadTabularFilesOptions additionalOptions(Map<String, String> options);

    ReadTabularFilesOptions groupBy(String columnName);

    ReadTabularFilesOptions aggregateColumns(String newColumnName, String... columns);
}
