package com.marklogic.newtool.api;

import java.util.function.Consumer;

/**
 * Top-level interface for objects that can execute some type of batch process involving a connection to MarkLogic.
 */
public interface Executor<T extends Executor> {

    void execute();

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
}
