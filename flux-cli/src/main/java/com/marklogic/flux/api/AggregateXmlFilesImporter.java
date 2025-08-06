/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read aggregate XML files from supported file locations with each row being written to MarkLogic.
 */
public interface AggregateXmlFilesImporter extends Executor<AggregateXmlFilesImporter> {

    interface ReadXmlFilesOptions extends ReadFilesOptions<ReadXmlFilesOptions> {
        ReadXmlFilesOptions element(String element);

        ReadXmlFilesOptions namespace(String namespace);

        ReadXmlFilesOptions uriElement(String uriElement);

        ReadXmlFilesOptions uriNamespace(String uriNamespace);

        ReadXmlFilesOptions compressionType(CompressionType compressionType);

        ReadXmlFilesOptions encoding(String encoding);

        ReadXmlFilesOptions partitions(int partitions);
    }

    AggregateXmlFilesImporter from(Consumer<ReadXmlFilesOptions> consumer);

    AggregateXmlFilesImporter from(String... paths);

    <T extends WriteDocumentsOptions<T>> AggregateXmlFilesImporter to(Consumer<T> consumer);
}
