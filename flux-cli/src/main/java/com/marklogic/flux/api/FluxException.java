package com.marklogic.flux.api;

/**
 * Wraps any exception not thrown by the MarkLogic Spark connector so that API users are not exposed directly to
 * Spark exceptions.
 */
public class FluxException extends RuntimeException {

    public FluxException(String message) {
        super(message);
    }

    public FluxException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
