/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class DocumentCopierTest extends AbstractTest {

    @Test
    void sameConnectionString() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content"))
            .to(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .batchSize(5)
                .logProgress(5)
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }

    @Test
    void withOutputConnection() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .from(options -> options.collections("author").categories("content"))
            .outputConnection(options -> options
                .host(getDatabaseClient().getHost())
                .port(getDatabaseClient().getPort())
                .username(DEFAULT_USER)
                .password(DEFAULT_PASSWORD))
            .to(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }
}
