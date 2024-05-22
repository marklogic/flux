package com.marklogic.newtool.api;

/**
 * Intended to wrap any non-MarkLogic-connector exception so that users - particularly API users - are not exposed
 * directly to e.g. Spark exceptions. Will be renamed once we have an official name.
 */
public class NtException extends RuntimeException {

    public NtException(String message) {
        super(message);
    }

    public NtException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
