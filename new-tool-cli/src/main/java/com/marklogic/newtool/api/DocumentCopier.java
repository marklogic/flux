package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface DocumentCopier extends Executor<DocumentCopier> {

    interface CopyReadDocumentsOptions extends ReadDocumentsOptions<CopyReadDocumentsOptions> {
        CopyReadDocumentsOptions categories(String... categories);
    }

    DocumentCopier readDocuments(Consumer<CopyReadDocumentsOptions> consumer);

    DocumentCopier outputConnection(Consumer<ConnectionOptions> consumer);

    DocumentCopier outputConnectionString(String connectionString);

    DocumentCopier writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
