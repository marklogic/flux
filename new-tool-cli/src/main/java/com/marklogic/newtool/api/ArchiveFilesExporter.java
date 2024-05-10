package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface ArchiveFilesExporter extends Executor<ArchiveFilesExporter> {

    interface ReadArchiveDocumentOptions extends ReadDocumentsOptions<ReadArchiveDocumentOptions> {
        ReadArchiveDocumentOptions categories(String... categories);
    }

    ArchiveFilesExporter readDocuments(Consumer<ReadArchiveDocumentOptions> consumer);

    ArchiveFilesExporter writeFiles(Consumer<WriteFilesOptions<? extends WriteFilesOptions>> consumer);
}
