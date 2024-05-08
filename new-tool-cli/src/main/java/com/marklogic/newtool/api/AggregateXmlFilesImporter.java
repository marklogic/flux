package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface AggregateXmlFilesImporter extends Executor<AggregateXmlFilesImporter> {

    interface ReadXmlFilesOptions extends ReadFilesOptions<ReadXmlFilesOptions> {
        ReadXmlFilesOptions element(String element);

        ReadXmlFilesOptions namespace(String namespace);

        ReadXmlFilesOptions uriElement(String uriElement);

        ReadXmlFilesOptions uriNamespace(String uriNamespace);

        ReadXmlFilesOptions compressionType(CompressionType compressionType);
    }

    AggregateXmlFilesImporter readFiles(Consumer<ReadXmlFilesOptions> consumer);

    AggregateXmlFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
