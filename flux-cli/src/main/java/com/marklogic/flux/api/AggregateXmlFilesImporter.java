/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read aggregate XML files from local, HDFS, and S3 locations with each row being written to MarkLogic.
 */
public interface AggregateXmlFilesImporter extends Executor<AggregateXmlFilesImporter> {

    interface ReadXmlFilesOptions extends ReadFilesOptions<ReadXmlFilesOptions> {
        ReadXmlFilesOptions element(String element);

        ReadXmlFilesOptions namespace(String namespace);

        ReadXmlFilesOptions uriElement(String uriElement);

        ReadXmlFilesOptions uriNamespace(String uriNamespace);

        ReadXmlFilesOptions compressionType(CompressionType compressionType);

        ReadXmlFilesOptions partitions(Integer partitions);
    }

    AggregateXmlFilesImporter from(Consumer<ReadXmlFilesOptions> consumer);

    AggregateXmlFilesImporter from(String... paths);

    AggregateXmlFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
