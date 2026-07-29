/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 archive files written by MLCP and write the documents in each archive to MarkLogic.
 */
public interface MlcpArchiveFilesImporter extends Executor<MlcpArchiveFilesImporter> {

    interface ReadMlcpArchiveFilesOptions extends ReadCompressibleFilesOptions<ReadMlcpArchiveFilesOptions> {
        ReadMlcpArchiveFilesOptions categories(String... categories);
        ReadMlcpArchiveFilesOptions encoding(String encoding);

    }

    MlcpArchiveFilesImporter from(Consumer<ReadMlcpArchiveFilesOptions> consumer);

    MlcpArchiveFilesImporter from(String... paths);

    <T extends WriteDocumentsOptions<T>> MlcpArchiveFilesImporter to(Consumer<T> consumer);
}
