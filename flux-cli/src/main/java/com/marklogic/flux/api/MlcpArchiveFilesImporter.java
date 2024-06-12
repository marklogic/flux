package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface MlcpArchiveFilesImporter extends Executor<MlcpArchiveFilesImporter> {

    interface ReadMlcpArchiveFilesOptions extends ReadFilesOptions<ReadMlcpArchiveFilesOptions> {
        ReadMlcpArchiveFilesOptions categories(String... categories);
        ReadMlcpArchiveFilesOptions partitions(Integer partitions);
    }

    MlcpArchiveFilesImporter readFiles(Consumer<ReadMlcpArchiveFilesOptions> consumer);

    MlcpArchiveFilesImporter readFiles(String... paths);

    MlcpArchiveFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
