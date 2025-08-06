/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Top-level interface for objects that can execute some type of batch process involving a connection to MarkLogic.
 */
public interface Executor<T extends Executor> {

    /**
     * Execute this executor instance with a default Spark session.
     */
    void execute();

    /**
     * Execute this executor with a user-defined Spark session.
     *
     * @param sparkSession must be an instance of {@code org.apache.spark.sql.SparkSession}. The Spark type is not used
     *                     here to avoid adding the Spark API and all of its dependencies to the compile-time
     *                     classpath of clients.
     * @return instance of this executor
     */
    T withSparkSession(Object sparkSession);

    /**
     * Configure a connection to MarkLogic.
     *
     * @param consumer Provided by the caller to configure the given options object.
     * @return instance of this executor
     */
    T connection(Consumer<ConnectionOptions> consumer);

    /**
     * Convenience for the common use case of configuring a MarkLogic connection via a connection string. Requires
     * that the associated MarkLogic REST API app server use basic or digest authentication.
     *
     * @param connectionString Defines a connection to MarkLogic via "user:password@host:port/optionalDatabaseName".
     * @return instance of this executor
     */
    T connectionString(String connectionString);

    /**
     * Limit the number of records read from the executor's data source.
     *
     * @param limit Maximum number of records to read from the executor's data source.
     * @return instance of this executor
     */
    T limit(int limit);

    /**
     * @param partitionCount the number of partitions to use for writing or processing the data that has been read.
     * @return instance of this executor
     * @since 1.2.0
     */
    T repartition(int partitionCount);

    /**
     * @return the count of rows to be read by this executor from its data source.
     */
    long count();
}
