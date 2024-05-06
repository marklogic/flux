package com.marklogic.newtool.api;

public interface GenericFilesImporter extends FilesImporter<GenericFilesImporter> {

    GenericFilesImporter withDocumentType(DocumentType documentType);
}
