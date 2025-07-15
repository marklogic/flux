/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

public interface ReadDocumentsOptions<T extends ReadDocumentsOptions> {

    T stringQuery(String stringQuery);

    T uris(String... uris);

    T query(String query);

    T options(String options);

    T collections(String... collections);

    T directory(String directory);

    T logProgress(int interval);

    T transform(String transform);

    T transformParams(String delimitedNamesAndValues);

    T transformParamsDelimiter(String delimiter);

    T batchSize(int batchSize);

    T partitionsPerForest(int partitionsPerForest);

    /**
     * Read documents at multiple points in time, as opposed to using a consistent snapshot.
     *
     * @since 1.1.2
     */
    T noSnapshot();

    /**
     * @since 1.4.0
     */
    T secondaryUrisJavaScript(String secondaryUrisJavaScript);

    /**
     * @since 1.4.0
     */
    T secondaryUrisXQuery(String secondaryUrisXQuery);

    /**
     * @since 1.4.0
     */
    T secondaryUrisJavaScriptFile(String secondaryUrisJavaScriptFile);

    /**
     * @since 1.4.0
     */
    T secondaryUrisXQueryFile(String secondaryUrisXQueryFile);

    /**
     * @since 1.4.0
     */
    T secondaryUrisInvoke(String secondaryUrisInvoke);

    /**
     * @since 1.4.0
     */
    T secondaryUrisVars(Map<String, String> secondaryUrisVars);
}
