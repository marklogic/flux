/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

/**
 * @since 1.2.0
 */
public interface SplitterOptions {

    SplitterOptions jsonPointers(String... jsonPointers);

    SplitterOptions xpath(String xpath);

    SplitterOptions xmlNamespaces(String... prefixesAndUris);

    SplitterOptions maxChunkSize(int maxChunkSize);

    SplitterOptions maxOverlapSize(int maxOverlapSize);

    SplitterOptions regex(String regex);

    SplitterOptions joinDelimiter(String joinDelimiter);

    SplitterOptions text();

    SplitterOptions documentSplitterClassName(String documentSplitterClassName);

    SplitterOptions documentSplitterClassOptions(Map<String, String> options);

    SplitterOptions outputMaxChunks(int maxChunks);

    SplitterOptions outputDocumentType(ChunkDocumentType documentType);

    SplitterOptions outputCollections(String... collections);

    SplitterOptions outputPermissionsString(String rolesAndCapabilities);

    SplitterOptions outputRootName(String rootName);

    SplitterOptions outputUriPrefix(String uriPrefix);

    SplitterOptions outputUriSuffix(String uriSuffix);

    SplitterOptions outputXmlNamespace(String xmlNamespace);

    SplitterOptions classifierHost(String classifierHost);

    SplitterOptions classifierPort(String classifierPort);

    SplitterOptions classifierHttps(Boolean classifierProtocol);

    SplitterOptions classifierEndpoint(String classifierEndpoint);

    SplitterOptions classifierApiKey(String classifierApiKey);

    SplitterOptions classifierTokenEndpoint(String classifierTokenEndpoint);

    SplitterOptions classifierConceptsArrayName(String classifierConceptsArrayName);

    enum ChunkDocumentType {
        JSON,
        XML
    }
}
