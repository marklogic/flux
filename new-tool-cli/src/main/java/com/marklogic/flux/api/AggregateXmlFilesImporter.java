package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface AggregateXmlFilesImporter extends Executor<AggregateXmlFilesImporter> {

    interface ReadXmlFilesOptions extends ReadFilesOptions<ReadXmlFilesOptions> {
        ReadXmlFilesOptions element(String element);

        ReadXmlFilesOptions namespace(String namespace);

        ReadXmlFilesOptions uriElement(String uriElement);

        ReadXmlFilesOptions uriNamespace(String uriNamespace);

        ReadXmlFilesOptions compressionType(CompressionType compressionType);

        ReadXmlFilesOptions partitions(Integer partitions);
    }

    AggregateXmlFilesImporter readFiles(Consumer<ReadXmlFilesOptions> consumer);

    AggregateXmlFilesImporter readFiles(String... paths);

    AggregateXmlFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
