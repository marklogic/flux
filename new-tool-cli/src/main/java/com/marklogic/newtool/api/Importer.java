package com.marklogic.newtool.api;

import com.marklogic.client.io.DocumentMetadataHandle;

import java.util.Map;
import java.util.Set;

/**
 * Base interface for executors that can import data as documents in MarkLogic.
 */
public interface Importer<T extends Executor> extends Executor<T> {

    T withCollections(String... collections);

    T withCollectionsString(String commaDelimitedCollections);

    T withPermissions(Map<String, Set<DocumentMetadataHandle.Capability>> permissions);

    T withPermissionsString(String rolesAndCapabilities);
}
