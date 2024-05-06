package com.marklogic.newtool.api;

import com.marklogic.client.io.DocumentMetadataHandle;

import java.util.Map;
import java.util.Set;

public interface GenericFilesImporter extends Executor<GenericFilesImporter> {

    GenericFilesImporter withPath(String path);

    GenericFilesImporter withCollections(String... collections);

    GenericFilesImporter withCollectionsString(String commaDelimitedCollections);

    GenericFilesImporter withPermissions(Map<String, Set<DocumentMetadataHandle.Capability>> permissions);

    GenericFilesImporter withPermissionsString(String rolesAndCapabilities);
}
