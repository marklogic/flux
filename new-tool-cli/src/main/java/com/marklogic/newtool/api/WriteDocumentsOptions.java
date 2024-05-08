package com.marklogic.newtool.api;

import com.marklogic.client.io.DocumentMetadataHandle;

import java.util.Map;
import java.util.Set;

public interface WriteDocumentsOptions<T extends WriteDocumentsOptions> {

    T abortOnFailure(Boolean value);

    T batchSize(int batchSize);

    T collections(String... collections);

    T collectionsString(String commaDelimitedCollections);

    T failedDocumentsPath(String path);

    T permissions(Map<String, Set<DocumentMetadataHandle.Capability>> permissions);

    T permissionsString(String rolesAndCapabilities);

    T temporalCollection(String temporalCollection);

    T threadCount(int threadCount);

    T transform(String transform);

    T transformParams(String delimitedNamesAndValues);

    T transformParamsDelimiter(String delimiter);

    T uriPrefix(String uriPrefix);

    T uriReplace(String uriReplace);

    T uriSuffix(String uriSuffix);
}
