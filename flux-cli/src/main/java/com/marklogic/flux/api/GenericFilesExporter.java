/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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

        /**
         * Enable use of the AWS profile credentials provider.
         *
         * @since 2.0.0
         */
        WriteGenericFilesOptions s3UseProfile();

        /**
         * @since 2.2.0
         */
        WriteGenericFilesOptions s3AnonymousAccess();

        WriteGenericFilesOptions s3AccessKeyId(String accessKeyId);

        WriteGenericFilesOptions s3SecretAccessKey(String secretAccessKey);

        WriteGenericFilesOptions s3Endpoint(String endpoint);

        /**
         * @since 2.0.0
         */
        WriteGenericFilesOptions s3Region(String region);

        /**
         * @since 1.4.0
         */
        WriteGenericFilesOptions azureStorage(Consumer<AzureStorageOptions> consumer);
    }

    <T extends ReadDocumentsOptions<T>> GenericFilesExporter from(Consumer<T> consumer);

    /**
     * @since 1.1.0
     */
    GenericFilesExporter streaming();

    GenericFilesExporter to(Consumer<WriteGenericFilesOptions> consumer);

    GenericFilesExporter to(String path);
}
