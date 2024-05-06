package com.marklogic.newtool.api;

public interface MlcpArchivesImporter extends FilesImporter<MlcpArchivesImporter> {

    MlcpArchivesImporter withCategoriesString(String commaDelimitedCategories);
}
