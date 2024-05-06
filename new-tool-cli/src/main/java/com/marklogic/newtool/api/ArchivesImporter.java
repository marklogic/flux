package com.marklogic.newtool.api;

public interface ArchivesImporter extends FilesImporter<ArchivesImporter> {

    ArchivesImporter withCategoriesString(String commaDelimitedCategories);
}
