package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class DocumentCopierTest extends AbstractTest {

    @Test
    void sameConnectionString() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .readDocuments(options -> options.collections("author").categories("content"))
            .writeDocuments(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }

    @Test
    void withOutputConnection() {
        Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .readDocuments(options -> options.collections("author").categories("content"))
            .outputConnection(options -> options
                .host(getDatabaseClient().getHost())
                .port(getDatabaseClient().getPort())
                .username(DEFAULT_USER)
                .password(DEFAULT_PASSWORD))
            .writeDocuments(options -> options
                .collections("author-copies")
                .uriPrefix("/copied")
                .permissionsString(DEFAULT_PERMISSIONS))
            .execute();

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
    }
}
