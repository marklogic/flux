package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface DocumentCopier extends Executor<DocumentCopier> {

    interface CopyReadDocumentsOptions extends ReadDocumentsOptions<CopyReadDocumentsOptions> {
        CopyReadDocumentsOptions categories(String... categories);
    }

    DocumentCopier from(Consumer<CopyReadDocumentsOptions> consumer);

    DocumentCopier outputConnection(Consumer<ConnectionOptions> consumer);

    DocumentCopier outputConnectionString(String connectionString);

    DocumentCopier to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}
