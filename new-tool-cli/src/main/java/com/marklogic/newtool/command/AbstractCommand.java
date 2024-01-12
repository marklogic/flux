package com.marklogic.newtool.command;

import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractCommand implements Command {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ParametersDelegate
    private CommonParams commonParams = new CommonParams();

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @Override
    public Optional<Preview> execute(SparkSession session) {
        long start = System.currentTimeMillis();
        try {
            DataFrameReader reader = session.read();
            Dataset<Row> dataset = loadDataset(session, reader);
            dataset = commonParams.applyParams(dataset);
            if (commonParams.isPreviewRequested()) {
                return Optional.of(commonParams.makePreview(dataset));
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
