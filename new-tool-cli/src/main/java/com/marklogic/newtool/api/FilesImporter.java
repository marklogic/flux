package com.marklogic.newtool.api;

/**
 * Base interface for objects that can import files as documents in MarkLogic.
 */
public interface FilesImporter<T extends FilesImporter> extends Importer<T> {

    T withPath(String path);

    T withFilter(String filter);

    T withRecursiveFileLookup(boolean value);
}
