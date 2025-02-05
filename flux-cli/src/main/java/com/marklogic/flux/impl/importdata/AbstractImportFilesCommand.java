/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.Executor;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.impl.AbstractCommand;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;

/**
 * Base class for commands that import files and write to MarkLogic.
 */
public abstract class AbstractImportFilesCommand<T extends Executor> extends AbstractCommand<T> {

    /**
     * Subclass must define the format used for reading - e.g. "csv", "marklogic", etc.
     *
     * @return the name of the Spark data source or connector to pass to the Spark 'format(String)' method
     */
    protected abstract String getReadFormat();

    protected abstract <RFP extends ReadFilesParams> RFP getReadParams();

    protected abstract <WDP extends WriteDocumentParams> WDP getWriteParams();

    @Override
    protected void validateDuringApiUsage() {
        if (!getReadParams().hasAtLeastOnePath()) {
            throw new FluxException("Must specify one or more file paths");
        }
    }

    @Override
    protected final Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        ReadFilesParams<?> readFilesParams = getReadParams();
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", readFilesParams.getPaths());
        }
        readFilesParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        Dataset<Row> dataset = reader
            .format(getReadFormat())
            .options(readFilesParams.makeOptions())
            .load(readFilesParams.getPaths().toArray(new String[]{}));

        // This is a little funky, how the splitter UDF stuff is in the writer params, but we invoke the UDF before
        // the writer starts.
        UserDefinedFunction textSplitter = getWriteParams().getSplitterParams().buildTextSplitter();
        if (textSplitter != null) {
            dataset.show();
            dataset = dataset.withColumn("chunks", textSplitter.apply(new Column("content")));
        }

        return dataset;
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
