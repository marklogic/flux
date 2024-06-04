package com.marklogic.newtool.impl;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.Executor;
import com.marklogic.newtool.api.NtException;
import com.marklogic.spark.ConnectorException;
import org.apache.spark.SparkException;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractCommand<T extends Executor> implements Command, Executor<T> {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger("com.marklogic.newtool");

    @ParametersDelegate
    private CommonParams commonParams = new CommonParams();

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @DynamicParameter(
        names = "-C",
        description = "Specify any key and value to be added to the Spark runtime configuration; e.g. -Cspark.logConf=true."
    )
    private Map<String, String> configParams = new HashMap<>();

    @Override
    public final Optional<Preview> execute(SparkSession session) {
        try {
            configParams.entrySet().stream().forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
            if (getConnectionParams().getSelectedHost() != null && logger.isInfoEnabled()) {
                logger.info("Will connect to MarkLogic host: {}", getConnectionParams().getSelectedHost());
            }
            long start = System.currentTimeMillis();
            Dataset<Row> dataset = loadDataset(session, session.read());
            dataset = commonParams.applyParams(dataset);
            if (commonParams.isPreviewRequested()) {
                return Optional.of(commonParams.makePreview(dataset));
            }
            applyWriter(session, dataset.write());
            if (logger.isInfoEnabled()) {
                logger.info("Execution time: {}s", (System.currentTimeMillis() - start) / 1000);
            }
        } catch (ConnectorException ex) {
            throw ex;
        } catch (Exception ex) {
            handleException(ex);
        }
        return Optional.empty();
    }

    private void handleException(Exception ex) {
        if (ex.getCause() instanceof ConnectorException) {
            // Our connector exceptions are expected to be helpful and friendly to the user.
            throw (ConnectorException) ex.getCause();
        }
        if (ex instanceof SparkException && ex.getCause() != null) {
            if (ex.getCause() instanceof SparkException && ex.getCause().getCause() != null) {
                // For some errors, Spark throws a SparkException that wraps a SparkException, and it's the
                // wrapped SparkException that has a more useful error
                throw new NtException(ex.getCause().getCause());
            }
            // The top-level SparkException message typically has a stacktrace in it that is not likely to be helpful.
            throw new NtException(ex.getCause());
        }
        throw new NtException(ex);
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer);

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    public CommonParams getCommonParams() {
        return commonParams;
    }

    /**
     * Entry point for using commands via the API instead of the CLI.
     */
    @Override
    public void execute() {
        connectionParams.validateConnectionString("connection string");
        execute(SparkUtil.buildSparkSession());
    }

    @Override
    public T connection(Consumer consumer) {
        consumer.accept(getConnectionParams());
        return (T) this;
    }

    @Override
    public T connectionString(String connectionString) {
        getConnectionParams().connectionString(connectionString);
        return (T) this;
    }

    @Override
    public T limit(int limit) {
        commonParams.setLimit(limit);
        return (T) this;
    }
}
