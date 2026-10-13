/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;

/**
 * Sanitizes the top-level column names of a structured data source before documents are constructed. Decoupled from
 * the Spark Dataset API so that implementations can be easily unit tested.
 */
interface ColumnNameSanitizer {

    /**
     * @param columnNames the original column names, in order
     * @return the sanitized column names, in the same order as the given column names
     * @throws com.marklogic.flux.api.FluxException if a sanitized name is empty or if two or more columns sanitize
     *                                              to the same name
     */
    String[] sanitize(String[] columnNames);

    /**
     * @return the sanitizer for the given strategy, or null if no sanitization should be performed
     */
    static ColumnNameSanitizer forStrategy(ColumnNameStrategy strategy) {
        if (ColumnNameStrategy.SIMPLE.equals(strategy)) {
            return new SimpleColumnNameSanitizer();
        }
        return null;
    }
}
