/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.copy;

import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyTest extends AbstractTest {

    @Test
    void sameDatabase() {
        run(
            "copy",
            "--categories", "content",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-collections", "author-copies",
            "--output-uri-prefix", "/copied",
            "--output-permissions", DEFAULT_PERMISSIONS
        );

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
        assertDirectoryCount("/copied/", 15);
    }

    @Test
    void withUris() {
        run(
            "copy",
            "--categories", "content",
            "--uris", "/author/author1.json\n/author/author2.json",
            "--connection-string", makeConnectionString(),
            "--output-collections", "author-copies",
            "--output-uri-prefix", "/copied",
            "--output-permissions", DEFAULT_PERMISSIONS
        );

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 2);
        assertDirectoryCount("/copied/", 2);
    }

    @Test
    void sameDatabaseWithMetadata() {
        run(
            "copy",
            "--collections", "author",
            "--partitions-per-forest", "1",
            "--categories", "content,metadata",
            "--connection-string", makeConnectionString(),
            "--output-connection-string", makeConnectionString(),
            // No need to specify permissions since they are included via "--categories".
            "--output-collections", "author-copies",
            "--output-uri-prefix", "/copied"
        );

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
        assertDirectoryCount("/copied/", 15);
    }

    @Test
    void badConnectionString() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-connection-string", "not@valid"
        ), "Invalid value for option '--output-connection-string': Invalid value for connection string; " +
            "must be username:password@host:port/optionalDatabaseName");
    }

    @Test
    void missingHost() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-port", "8000"
        ), "Must specify a MarkLogic host via --output-host or --output-connection-string.");
    }

    @Test
    void missingPort() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-host", "localhost"
        ), "Must specify a MarkLogic app server port via --output-port or --output-connection-string.");
    }

    @Test
    void missingUsername() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-host", "localhost",
            "--output-port", "8000"
        ), "Must specify a MarkLogic user via --output-username when using 'BASIC' or 'DIGEST' authentication.");
    }

    @Test
    void missingPassword() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-host", "localhost",
            "--output-port", "8000",
            "--output-username", "someone"
        ), "Must specify a password via --output-password when using 'BASIC' or 'DIGEST' authentication.");
    }

    private void assertDirectoryCount(String directoryPrefix, int expectedCount) {
        QueryManager queryManager = getDatabaseClient().newQueryManager();
        StructuredQueryDefinition query = queryManager.newStructuredQueryBuilder().directory(true, directoryPrefix);
        SearchHandle results = queryManager.search(query, new SearchHandle());
        assertEquals(expectedCount, results.getTotalResults());
    }
}
