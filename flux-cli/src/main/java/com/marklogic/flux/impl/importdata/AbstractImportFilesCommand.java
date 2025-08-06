/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.Executor;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.impl.AbstractCommand;
import org.apache.spark.sql.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class for commands that import files and write to MarkLogic.
 */
public abstract class AbstractImportFilesCommand<T extends Executor<T>> extends AbstractCommand<T> {

    /**
     * Subclass must define the format used for reading - e.g. "csv", "marklogic", etc.
     *
     * @return the name of the Spark data source or connector to pass to the Spark 'format(String)' method
     */
    protected abstract String getReadFormat();

    protected abstract IReadFilesParams getReadParams();

    protected abstract Supplier<Map<String, String>> getWriteParams();

    @Override
    protected void validateDuringApiUsage() {
        List<String> paths = getReadParams().getPaths();
        if (paths == null || paths.isEmpty()) {
            throw new FluxException("Must specify one or more file paths");
        }
    }

    @Override
    protected final Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        IReadFilesParams readFilesParams = getReadParams();

        final List<String> transformedPaths = applyCloudStorageParams(
            session.sparkContext().hadoopConfiguration(),
            readFilesParams, readFilesParams.getPaths()
        );

        return reader
            .format(getReadFormat())
            .options(readFilesParams.makeOptions())
            .load(transformedPaths.toArray(new String[]{}));
    }

    @Override
    protected final void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(getWriteParams().get())
            .mode(SaveMode.Append)
            .save();
    }
}
