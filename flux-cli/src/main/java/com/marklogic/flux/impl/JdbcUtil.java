/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.FluxException;

public interface JdbcUtil {

    static Exception massageException(Exception ex) {
        if (ex instanceof ClassNotFoundException) {
            return new FluxException(String.format("Unable to load class: %s; for a JDBC driver, ensure you " +
                "are specifying the fully-qualified class name for your JDBC driver.", ex.getMessage()), ex);
        }
        return ex;
    }
}
