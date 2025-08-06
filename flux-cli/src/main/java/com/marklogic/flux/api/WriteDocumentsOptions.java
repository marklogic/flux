/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

public interface WriteDocumentsOptions<T extends WriteDocumentsOptions> {

    T abortOnWriteFailure(boolean value);

    T batchSize(int batchSize);

    T collections(String... collections);

    T collectionsString(String commaDelimitedCollections);

    T failedDocumentsPath(String path);

    T logProgress(int interval);

    T permissionsString(String rolesAndCapabilities);

    /**
     * @since 1.3.0
     */
    T pipelineBatchSize(int batchSize);

    /**
     * @since 1.2.0
     */
    T splitter(Consumer<SplitterOptions> consumer);

    /**
     * @since 1.2.0
     */
    T embedder(Consumer<EmbedderOptions> consumer);

    T temporalCollection(String temporalCollection);

    T threadCount(int threadCount);

    T threadCountPerPartition(int threadCountPerPartition);

    T transform(String transform);

    T transformParams(String delimitedNamesAndValues);

    T transformParamsDelimiter(String delimiter);

    T uriPrefix(String uriPrefix);

    T uriReplace(String uriReplace);

    T uriSuffix(String uriSuffix);

    T uriTemplate(String uriTemplate);

    /**
     * @since 1.3.0
     */
    T classifier(Consumer<ClassifierOptions> consumer);

    /**
     * @since 1.3.0
     */
    T metadataValues(Map<String, String> metadataValues);

    /**
     * @since 1.3.0
     */
    T documentProperties(Map<String, String> documentProperties);
}
