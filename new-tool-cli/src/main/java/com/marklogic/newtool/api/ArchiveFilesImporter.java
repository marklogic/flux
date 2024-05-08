package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface ArchiveFilesImporter extends Executor<ArchiveFilesImporter> {

    interface ReadArchiveFilesOptions extends ReadFilesOptions<ReadArchiveFilesOptions> {
        ReadArchiveFilesOptions categories(String... categories);
    }

    ArchiveFilesImporter readFiles(Consumer<ReadArchiveFilesOptions> consumer);

    ArchiveFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
