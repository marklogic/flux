package com.marklogic.newtool.api;

public interface JsonFilesImporter extends FilesImporter<JsonFilesImporter> {

    /**
     * @param value If true, then each file is expected to corresponds to the JSON Lines format with one JSON object
     *              per line.
     */
    JsonFilesImporter withJsonLines(boolean value);

    /**
     * @param jsonRootName Name of a root field to add to each JSON document.
     */
    JsonFilesImporter withJsonRootName(String jsonRootName);
}
