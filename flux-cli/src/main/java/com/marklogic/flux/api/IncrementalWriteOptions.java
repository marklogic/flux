/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Configures incremental writing, which avoids writing documents that have not changed since they were last
 * written to MarkLogic.
 *
 * @since 2.2.0
 */
public interface IncrementalWriteOptions {

    /**
     * @param schema Name of the schema to use for detecting changes when performing incremental writes.
     * @param view   Name of the view to use for detecting changes when performing incremental writes.
     */
    IncrementalWriteOptions schemaAndView(String schema, String view);

    /**
     * @param hashName Name of the metadata key in which to store the hash value used for detecting changes during
     *                 incremental writes. If using a schema and view to retrieve hashes, this is also the name of the
     *                 column in the view containing the hash. Defaults to "incrementalWriteHash".
     */
    IncrementalWriteOptions hashName(String hashName);

    /**
     * @param timestampName Name of the optional metadata key in which to store the timestamp for when the document
     *                      was written. No default value.
     */
    IncrementalWriteOptions timestampName(String timestampName);

    /**
     * Disables JSON canonicalization during incremental write. This should only be used when you are certain that
     * the JSON documents you write to MarkLogic do not require canonicalization - e.g. the order of keys in each
     * document is guaranteed to be the same every time.
     */
    IncrementalWriteOptions disableJsonCanonicalization();

    /**
     * @param jsonPointers JSON Pointer expressions for JSON content to exclude when calculating the hash value.
     */
    IncrementalWriteOptions jsonExclusions(String... jsonPointers);

    /**
     * @param xpathExpressions XPath expressions for XML content to exclude when calculating the hash value. Any
     *                         namespace prefixes can be registered via
     *                         {@link SplitterOptions#xmlNamespaces(String...)}.
     */
    IncrementalWriteOptions xmlExclusions(String... xpathExpressions);

    /**
     * @param interval Log a count of skipped documents every time this many documents are skipped. Defaults to
     *                 10,000; a value of 0 disables this logging.
     */
    IncrementalWriteOptions logSkipped(int interval);
}
