package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface MlcpArchiveFilesImporter extends Executor<MlcpArchiveFilesImporter> {

    interface ReadMlcpArchiveFilesOptions extends ReadFilesOptions<ReadMlcpArchiveFilesOptions> {
        ReadMlcpArchiveFilesOptions categories(String... categories);
        ReadMlcpArchiveFilesOptions partitions(Integer partitions);
    }

    MlcpArchiveFilesImporter from(Consumer<ReadMlcpArchiveFilesOptions> consumer);

    MlcpArchiveFilesImporter from(String... paths);

    MlcpArchiveFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
