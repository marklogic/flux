/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read documents from MarkLogic and write them to a local filesystem, HDFS, or S3.
 */
public interface GenericFilesExporter extends Executor<GenericFilesExporter> {

    interface WriteGenericFilesOptions {
        WriteGenericFilesOptions path(String path);

        WriteGenericFilesOptions compressionType(CompressionType compressionType);

        WriteGenericFilesOptions prettyPrint(boolean value);

        WriteGenericFilesOptions encoding(String encoding);

        WriteGenericFilesOptions zipFileCount(int zipFileCount);

        WriteGenericFilesOptions s3AddCredentials();

        WriteGenericFilesOptions s3AccessKeyId(String accessKeyId);

        WriteGenericFilesOptions s3SecretAccessKey(String secretAccessKey);

        WriteGenericFilesOptions s3Endpoint(String endpoint);
    }

    GenericFilesExporter from(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer);

    GenericFilesExporter to(Consumer<WriteGenericFilesOptions> consumer);

    GenericFilesExporter to(String path);
}
