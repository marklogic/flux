/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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

import java.util.function.Consumer;

public abstract class AbstractCommand<T extends Executor> implements Command, Executor<T> {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";
    protected static final Logger logger = LoggerFactory.getLogger("com.marklogic.flux");

    // The order values of 1 and 3 allow for CopyCommand to include Output Connection Options in between them. Feel free
    // to tweak these in the future as needed.
    @CommandLine.ArgGroup(exclusive = false, heading = "%nConnection Options%n", order = 1)
    private ConnectionParams connectionParams = new ConnectionParams();

    @CommandLine.ArgGroup(exclusive = false, heading = "%nCommon Options%n", order = 3)
    private CommonParams commonParams = new CommonParams();

    private SparkSession sparkSession;

    public final String determineSparkMasterUrl() {
        if (commonParams.getSparkMasterUrl() != null) {
            return commonParams.getSparkMasterUrl();
        }
        if (commonParams.getRepartition() > 0) {
            return SparkUtil.makeSparkMasterUrl(commonParams.getRepartition());
        }
        String url = getCustomSparkMasterUrl();
        return url != null ? url : "local[*]";
    }

    protected String getCustomSparkMasterUrl() {
        // Allows subclasses to provide their own Spark master URL based on command-specific options.
        return null;
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        new ConnectionParamsValidator(false).validate(connectionParams);
    }

    @Override
    public final void execute(SparkSession session) {
        try {
            commonParams.getSparkSessionConfigParams().entrySet().stream()
                .forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
            if (getConnectionParams().getSelectedHost() != null && logger.isInfoEnabled()) {
                logger.info("Will connect to MarkLogic host: {}", getConnectionParams().getSelectedHost());
            }
            long start = System.currentTimeMillis();
            Dataset<Row> dataset = readDataset(session);
            if (commonParams.isCount()) {
                logger.info("Count: {}", dataset.count());
            } else if (commonParams.getPreview().isPreviewRequested()) {
                commonParams.getPreview().showPreview(dataset);
            } else {
                applyWriter(session, dataset.write());
            }
            if (logger.isInfoEnabled()) {
                logger.info("Execution time: {}s", (System.currentTimeMillis() - start) / 1000);
            }
        } catch (ConnectorException ex) {
            throw ex;
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Entry point for using commands via the API instead of the CLI.
     */
    @Override
    public void execute() {
        SparkSession session = prepareApiExecution();
        execute(session);
    }

    @Override
    public long count() {
        try {
            SparkSession session = prepareApiExecution();
            return readDataset(session).count();
        } catch (ConnectorException ex) {
            throw ex;
        } catch (Exception ex) {
            handleException(ex);
            return 0;
        }
    }

    /**
     * Captures common logic for the normal API execute call and for the API count call. Expected to be usable for
     * future API calls like count() that need to invoke a method on the Spark Dataset.
     *
     * @return
     */
    private SparkSession prepareApiExecution() {
        connectionParams.validateConnectionString("connection string");
        validateDuringApiUsage();
        return this.sparkSession != null ? this.sparkSession : SparkUtil.buildSparkSession();
    }

    /**
     * Handles reading a dataset, which includes loading it via the subclass and applying some of the common params
     * to it as well. Intended to be reused by CLI command execution and for API calls like count() that only need
     * to read a Dataset and then call some method on it.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private Dataset<Row> readDataset(SparkSession session) throws Exception {
        Dataset<Row> dataset = loadDataset(session, session.read());
        dataset = afterDatasetLoaded(dataset);
        return commonParams.applyParams(dataset);
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

    /**
     * This where the subclass defines how data is actually read from a source and loaded into a Spark dataset.
     */
    @SuppressWarnings("java:S112")
    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) throws Exception;

    /**
     * This is where the subclass defines how the Spark dataset is written to some destination.
     */
    @SuppressWarnings("java:S112")
    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer) throws Exception;

    public final ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    public final CommonParams getCommonParams() {
        return commonParams;
    }

    /**
     * Because we cannot reuse validation expressed via picocli annotations - such as "required=true" - some
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

    @Override
    public T repartition(int partitionCount) {
        commonParams.setRepartition(partitionCount);
        return (T) this;
    }
}
