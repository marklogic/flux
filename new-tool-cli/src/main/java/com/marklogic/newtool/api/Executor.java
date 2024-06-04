package com.marklogic.newtool.api;

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
     */
    void executeWithSession(Object sparkSession);

    /**
     * @param consumer Provided by the caller to configure the given options object.
     * @return instance of this executor
     */
    T connection(Consumer<ConnectionOptions> consumer);

    /**
     * Convenience for the common use case of configuring a MarkLogic connection via a connection string.
     *
     * @param connectionString Defines a connection to MarkLogic via "user:password@host:port".
     * @return instance of this executor
     */
    T connectionString(String connectionString);

    /**
     * @param limit Maximum number of records to read from the executor's data source.
     * @return instance of this executor
     */
    T limit(int limit);
}
