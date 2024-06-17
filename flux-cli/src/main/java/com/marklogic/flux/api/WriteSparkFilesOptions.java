/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

/**
 * For objects that can export files via a Spark data source, where the Spark data source supports additional options
 * that do not have dedicated methods in the exporter interface.
 */
public interface WriteSparkFilesOptions extends WriteFilesOptions<WriteSparkFilesOptions> {

    WriteSparkFilesOptions additionalOptions(Map<String, String> options);
}
