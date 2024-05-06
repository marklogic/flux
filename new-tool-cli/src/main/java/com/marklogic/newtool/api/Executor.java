package com.marklogic.newtool.api;

import com.marklogic.newtool.command.Preview;

import java.util.Optional;

/**
 * Top-level interface for objects that can execute some type of batch process involving a connection to MarkLogic.
 */
public interface Executor<T extends Executor> {

    /**
     * @return an optional preview of the data read by the command, before any data is written.
     */
    Optional<Preview> execute();

    T withConnectionString(String connectionString);
}
