package com.marklogic.flux.api;

/**
 * Intended to wrap any non-MarkLogic-connector exception so that users - particularly API users - are not exposed
 * directly to e.g. Spark exceptions. Will be renamed once we have an official name.
 */
public class FluxException extends RuntimeException {

    public FluxException(String message) {
        super(message);
    }

    public FluxException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
