/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.Executor;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.spark.Util;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.sql.*;
import picocli.CommandLine;

/**
 * Support class for concrete commands that want to run an Optic DSL query to read rows and then write them to one or
 * more files. Each subclass is expected to make use of a Spark data source for writing rows to a tabular data source.
 */
abstract class AbstractExportRowsToFilesCommand<T extends Executor> extends AbstractCommand<T> {

    @CommandLine.Mixin
    protected final ReadRowsParams readParams = new ReadRowsParams();

    // Sonar complaining about the use of ?; not sure yet how to "fix" it, so ignoring.
    @SuppressWarnings("java:S1452")
    protected abstract WriteStructuredFilesParams<? extends WriteFilesOptions> getWriteFilesParams();

    /**
     * @return subclass must return the Spark data format for the desired output file.
     */
    protected abstract String getWriteFormat();

    @Override
    protected void validateDuringApiUsage() {
        getWriteFilesParams().validatePath();
    }

    @Override
    protected String getCustomSparkMasterUrl() {
        return readParams.getPartitions() > 0 ? SparkUtil.makeSparkMasterUrl(readParams.getPartitions()) : null;
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        final Integer fileCount = getWriteFilesParams().getFileCount();
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        Configuration hadoopConf = session.sparkContext().hadoopConfiguration();
        disableWriteChecksum(hadoopConf);

        WriteStructuredFilesParams<? extends WriteFilesOptions> writeParams = getWriteFilesParams();
        String outputPath = configureCloudStorageAccess(hadoopConf, writeParams);

        writer.format(getWriteFormat())
            .options(writeParams.get())
            .mode(SparkUtil.toSparkSaveMode(writeParams.getSaveMode()))
            .save(outputPath);
    }

    /**
     * Spark defaults to writing checksum files, which is going to be unfamiliar for Flux users that either aren't
     * familiar with Spark and/or do not expect this having used MLCP before. Additionally, the other export commands
     * that our the MarkLogic connector do not offer any support for checksum files. So disabling this feature for now.
     * May expose it via an option later based on feedback.
     *
     * @param hadoopConf
     */
    private void disableWriteChecksum(Configuration hadoopConf) {
        try {
            FileSystem.get(hadoopConf).setWriteChecksum(false);
        } catch (Exception e) {
            Util.MAIN_LOGGER.warn("Unable to disable writing Spark checksum files; cause: {}", e.getMessage());
        }
    }
}
