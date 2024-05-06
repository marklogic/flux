package com.marklogic.newtool.api;

import com.marklogic.newtool.command.importdata.ImportFilesCommand;

public interface NT {

    /**
     * @return an object that can import any type of file as-is, with the document type being determined by
     * the file extension.
     */
    static GenericFilesImporter importGenericFiles() {
        return new ImportFilesCommand();
    }
}
