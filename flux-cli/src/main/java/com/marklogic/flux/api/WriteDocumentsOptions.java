/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface WriteDocumentsOptions<T extends WriteDocumentsOptions> {

    T abortOnWriteFailure(Boolean value);

    T batchSize(int batchSize);

    T collections(String... collections);

    T collectionsString(String commaDelimitedCollections);

    T failedDocumentsPath(String path);

    T permissionsString(String rolesAndCapabilities);

    T temporalCollection(String temporalCollection);

    T threadCount(int threadCount);

    T totalThreadCount(int totalThreadCount);

    T transform(String transform);

    T transformParams(String delimitedNamesAndValues);

    T transformParamsDelimiter(String delimiter);

    T uriPrefix(String uriPrefix);

    T uriReplace(String uriReplace);

    T uriSuffix(String uriSuffix);

    T uriTemplate(String uriTemplate);
}
