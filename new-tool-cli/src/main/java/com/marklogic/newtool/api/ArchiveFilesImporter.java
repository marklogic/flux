package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface ArchiveFilesImporter extends Executor<ArchiveFilesImporter> {

    interface ReadArchiveFilesOptions extends ReadFilesOptions<ReadArchiveFilesOptions> {
        ReadArchiveFilesOptions categories(String... categories);
        ReadArchiveFilesOptions partitions(Integer partitions);
    }

    ArchiveFilesImporter readFiles(Consumer<ReadArchiveFilesOptions> consumer);

    ArchiveFilesImporter readFiles(String... paths);

    ArchiveFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
