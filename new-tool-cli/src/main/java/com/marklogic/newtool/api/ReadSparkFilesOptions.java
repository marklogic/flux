package com.marklogic.newtool.api;

import java.util.Map;

/**
 * For objects that can import files via a Spark data source, where the Spark data source supports additional options
 * that do not have dedicated methods in the importer interface.
 */
public interface ReadSparkFilesOptions extends ReadFilesOptions<ReadSparkFilesOptions> {

    ReadSparkFilesOptions additionalOptions(Map<String, String> options);
}
