package com.marklogic.newtool.command;

import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractCommand implements Command {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger("com.marklogic.newtool");

    @ParametersDelegate
    private CommonParams commonParams = new CommonParams();

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @Override
    public final Optional<Preview> execute(SparkSession session) {
        modifySparkSession(session);

        String host = getConnectionParams().getSelectedHost();
        if (host != null && logger.isInfoEnabled()) {
            logger.info("Will connect to MarkLogic host: {}", host);
        }

        long start = System.currentTimeMillis();

        DataFrameReader reader = session.read();
        Dataset<Row> dataset = loadDataset(session, reader);

        dataset = commonParams.applyParams(dataset);
        if (commonParams.isPreviewRequested()) {
            return Optional.of(commonParams.makePreview(dataset));
        }

        DataFrameWriter<Row> writer = dataset.write();
        applyWriter(session, writer);
        if (logger.isInfoEnabled()) {
            logger.info("Execution time: {}s", (System.currentTimeMillis() - start) / 1000);
        }

        return Optional.empty();
    }

    protected void modifySparkSession(SparkSession session) {
        // Subclasses can override this to e.g. modify the Spark configuration.
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer);

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }
}
