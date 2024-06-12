package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface ArchiveFilesExporter extends Executor<ArchiveFilesExporter> {

    interface ReadArchiveDocumentOptions extends ReadDocumentsOptions<ReadArchiveDocumentOptions> {
        ReadArchiveDocumentOptions categories(String... categories);
    }

    ArchiveFilesExporter from(Consumer<ReadArchiveDocumentOptions> consumer);

    ArchiveFilesExporter to(Consumer<WriteFilesOptions<? extends WriteFilesOptions>> consumer);

    ArchiveFilesExporter to(String path);
}
