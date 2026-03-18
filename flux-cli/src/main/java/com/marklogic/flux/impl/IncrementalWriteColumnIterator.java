/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.tde.TdeBuilder;

import java.util.Iterator;

/**
 * Wraps an existing {@link Iterator} of {@link TdeBuilder.Column} objects and prepends the two columns required
 * for the incremental write schema-and-view approach: a {@code uri} column first, then a hash column.
 */
class IncrementalWriteColumnIterator implements Iterator<TdeBuilder.Column> {

    private final Iterator<TdeBuilder.Column> delegate;
    private final String hashName;

    // 0 = uri column next, 1 = hash column next, 2 = delegate
    private int preambleIndex = 0;

    IncrementalWriteColumnIterator(Iterator<TdeBuilder.Column> delegate, String hashName) {
        this.delegate = delegate;
        this.hashName = hashName;
    }

    @Override
    public boolean hasNext() {
        return preambleIndex < 2 || delegate.hasNext();
    }

    @Override
    public TdeBuilder.Column next() {
        if (preambleIndex == 0) {
            preambleIndex++;
            return new SyntheticColumn("uri", "string", "xdmp:node-uri(.)", false);
        }
        if (preambleIndex == 1) {
            preambleIndex++;
            return new SyntheticColumn(hashName, "unsignedLong",
                String.format("xdmp:node-metadata-value(., '%s')", hashName), true);
        }
        return delegate.next();
    }

    private record SyntheticColumn(String name, String scalarType, String val, boolean nullable)
        implements TdeBuilder.Column {
        @Override public String getName() { return name; }
        @Override public String getScalarType() { return scalarType; }
        @Override public String getVal() { return val; }
        @Override public boolean isNullable() { return nullable; }
    }
}
