package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCommand implements Command {

    protected final static String MARKLOGIC_CONNECTOR = "com.marklogic.spark";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @Parameter(names = "--partitions", description = "number of Spark partitions")
    private Integer partitions;

    @Parameter(names = "--preview", description = "Set to true to log the dataset instead of writing it.")
    private boolean preview;

    // TODO Apply optionsFile here.
    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        long start = System.currentTimeMillis();
        try {
            DataFrameReader reader = session.read();
            Dataset<Row> dataset = loadDataset(session, reader);
            if (partitions != null) {
                dataset = dataset.repartition(partitions);
            }
            if (preview) {
                return Optional.of(dataset.collectAsList());
            }
            DataFrameWriter<Row> writer = dataset.write();
            applyWriter(session, writer);
            return Optional.empty();
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("Completed, duration in ms: " + (System.currentTimeMillis() - start));
            }
        }
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer);

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }
}
