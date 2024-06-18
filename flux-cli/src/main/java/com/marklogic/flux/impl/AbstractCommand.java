/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.Executor;
import com.marklogic.flux.api.FluxException;
import com.marklogic.spark.ConnectorException;
import org.apache.spark.SparkException;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractCommand<T extends Executor> implements Command, Executor<T> {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger("com.marklogic.flux");

    @CommandLine.ArgGroup(exclusive = false, heading = "Common Options\n")
    private CommonParams commonParams = new CommonParams();

    @CommandLine.ArgGroup(exclusive = false, heading = "Connection Options\n")
    private ConnectionParams connectionParams = new ConnectionParams();

    private SparkSession sparkSession;

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        new ConnectionParamsValidator(false).validate(connectionParams, commonParams);
    }

    @Override
    public final Optional<Preview> execute(SparkSession session) {
        try {
            commonParams.getConfigParams().entrySet().stream()
                .forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
            if (getConnectionParams().getSelectedHost() != null && logger.isInfoEnabled()) {
                logger.info("Will connect to MarkLogic host: {}", getConnectionParams().getSelectedHost());
            }
            long start = System.currentTimeMillis();
            Dataset<Row> dataset = loadDataset(session, session.read());
            dataset = afterDatasetLoaded(dataset);
            dataset = commonParams.applyParams(dataset);
            if (commonParams.isCount()) {
                logger.info("Count of rows read: {}", dataset.count());
            } else if (commonParams.isPreviewRequested()) {
                return Optional.of(commonParams.makePreview(dataset));
            } else {
                applyWriter(session, dataset.write());
                if (logger.isInfoEnabled()) {
                    logger.info("Execution time: {}s", (System.currentTimeMillis() - start) / 1000);
                }
            }
        } catch (ConnectorException ex) {
            throw ex;
        } catch (Exception ex) {
            handleException(ex);
        }
        return Optional.empty();
    }

    /**
     * Allows a subclass to modify the dataset after "load()" has been called but before "write()" is called.
     *
     * @param dataset
     * @return
     */
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        return dataset;
    }

    private void handleException(Exception ex) {
        if (ex.getCause() instanceof ConnectorException) {
            // Our connector exceptions are expected to be helpful and friendly to the user.
            throw (ConnectorException) ex.getCause();
        }
        if (ex instanceof FluxException) {
            throw (FluxException) ex;
        }
        if (ex instanceof SparkException && ex.getCause() != null) {
            if (ex.getCause() instanceof SparkException && ex.getCause().getCause() != null) {
                // For some errors, Spark throws a SparkException that wraps a SparkException, and it's the
                // wrapped SparkException that has a more useful error
                throw new FluxException(ex.getCause().getCause());
            }
            // The top-level SparkException message typically has a stacktrace in it that is not likely to be helpful.
            throw new FluxException(ex.getCause());
        }
        // The exception class name is included in the hopes that it will provide some helpful context without having
        // to ask for the stacktrace to be shown.
        throw new FluxException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
    }

    @SuppressWarnings("java:S112")
    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) throws Exception;

    @SuppressWarnings("java:S112")
    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer) throws Exception;

    public final ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    public final CommonParams getCommonParams() {
        return commonParams;
    }

    /**
     * Entry point for using commands via the API instead of the CLI.
     */
    @Override
    public void execute() {
        doExecute();
    }

    /**
     * Extracted so that it can be reused by {@code count()} and any future API methods that want access to the Dataset.
     *
     * @return
     */
    private Optional<Preview> doExecute() {
        connectionParams.validateConnectionString("connection string");
        validateDuringApiUsage();
        return execute(this.sparkSession != null ? this.sparkSession : SparkUtil.buildSparkSession());
    }

    /**
     * Because we cannot reuse validation expressed via JCommander annotations - such as "required=true" - some
     * subclasses may need to perform their own validation when the user is using the API.
     */
    protected void validateDuringApiUsage() {
        // Intended to be overridden by subclass as needed.
    }

    @Override
    public T withSparkSession(Object sparkSession) {
        if (!(sparkSession instanceof SparkSession)) {
            throw new FluxException("The session object must be an instance of org.apache.spark.sql.SparkSession");
        }
        this.sparkSession = (SparkSession) sparkSession;
        return (T) this;
    }

    @Override
    public long count() {
        // The actual preview value doesn't matter; we set this so that we can get back the Dataset without
        // writing any data.
        getCommonParams().setPreview(Integer.MAX_VALUE);
        Optional<Preview> preview = doExecute();
        return preview.isPresent() ? preview.get().getDataset().count() : 0;
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
