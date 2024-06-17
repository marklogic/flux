/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read local, HDFS, and S3 archive files written by MLCP and write the documents in each archive to MarkLogic.
 */
public interface MlcpArchiveFilesImporter extends Executor<MlcpArchiveFilesImporter> {

    interface ReadMlcpArchiveFilesOptions extends ReadFilesOptions<ReadMlcpArchiveFilesOptions> {
        ReadMlcpArchiveFilesOptions categories(String... categories);
        ReadMlcpArchiveFilesOptions partitions(Integer partitions);
    }

    MlcpArchiveFilesImporter from(Consumer<ReadMlcpArchiveFilesOptions> consumer);

    MlcpArchiveFilesImporter from(String... paths);

    MlcpArchiveFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
