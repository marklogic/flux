/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

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

    T classifier(Consumer<ClassifierOptions> consumer);
}
