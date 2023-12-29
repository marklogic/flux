package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractCommand implements Command {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @Parameter(names = "--partitions", description = "number of Spark partitions")
    private Integer partitions;

    @Parameter(names = "--preview", description = "Show up to the first N rows of data read by the command.")
    private Integer preview;

    @Parameter(names = "--previewDrop", description = "Specify one or more columns to drop when using --preview.", variableArity = true)
    private List<String> previewColumnsToDrop = new ArrayList<>();

    @Override
    public Optional<Preview> execute(SparkSession session) {
        long start = System.currentTimeMillis();
        try {
            DataFrameReader reader = session.read();
            Dataset<Row> dataset = loadDataset(session, reader);
            if (partitions != null) {
                dataset = dataset.repartition(partitions);
            }
            if (preview != null) {
                return Optional.of(new Preview(dataset, preview, previewColumnsToDrop));
            }
            DataFrameWriter<Row> writer = dataset.write();
            applyWriter(session, writer);
            return Optional.empty();
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("Completed, duration in ms: {}", System.currentTimeMillis() - start);
            }
        }
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer);

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }
}
