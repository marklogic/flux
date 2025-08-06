/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Copy documents from one database to another database, which can also be the originating database.
 */
public interface DocumentCopier extends Executor<DocumentCopier> {

    interface CopyReadDocumentsOptions extends ReadDocumentsOptions<CopyReadDocumentsOptions> {
        CopyReadDocumentsOptions categories(String... categories);
    }

    interface CopyWriteDocumentsOptions extends WriteDocumentsOptions<CopyWriteDocumentsOptions> {
    }

    DocumentCopier from(Consumer<CopyReadDocumentsOptions> consumer);

    DocumentCopier outputConnection(Consumer<ConnectionOptions> consumer);

    DocumentCopier outputConnectionString(String connectionString);

    DocumentCopier to(Consumer<CopyWriteDocumentsOptions> consumer);
}
