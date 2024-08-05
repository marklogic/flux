/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows from MarkLogic and write them using a custom Spark connector or data source.
 */
public interface CustomRowsExporter extends Executor<CustomRowsExporter> {

    CustomRowsExporter from(Consumer<ReadRowsOptions> consumer);

    CustomRowsExporter from(String opticQuery);

    CustomRowsExporter to(Consumer<CustomExportWriteOptions> consumer);
}
