package com.marklogic.newtool.api;

public interface AggregateXmlFilesImporter extends FilesImporter<AggregateXmlFilesImporter> {

    AggregateXmlFilesImporter withElement(String element);

    AggregateXmlFilesImporter withNamespace(String namespace);
}
