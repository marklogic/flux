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
            "--connectionString", makeConnectionString(),
            "--outputCollections", "author-copies",
            "--outputUriPrefix", "/copied",
            "--outputPermissions", DEFAULT_PERMISSIONS
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
            "--connectionString", makeConnectionString(),
            "--outputCollections", "author-copies",
            "--outputUriPrefix", "/copied",
            "--outputPermissions", DEFAULT_PERMISSIONS
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
            "--partitionsPerForest", "1",
            "--categories", "content,metadata",
            "--connectionString", makeConnectionString(),
            "--outputConnectionString", makeConnectionString(),
            // No need to specify permissions since they are included via "--categories".
            "--outputCollections", "author-copies",
            "--outputUriPrefix", "/copied"
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
            "--connectionString", makeConnectionString(),
            "--outputConnectionString", "not@valid"
        ), "Invalid value for --outputConnectionString; must be username:password@host:port/optionalDatabaseName");
    }

    @Test
    void missingHost() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connectionString", makeConnectionString(),
            "--outputPort", "8000"
        ), "Must specify a MarkLogic host via --outputHost or --outputConnectionString.");
    }

    @Test
    void missingPort() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connectionString", makeConnectionString(),
            "--outputHost", "localhost"
        ), "Must specify a MarkLogic app server port via --outputPort or --outputConnectionString.");
    }

    @Test
    void missingUsername() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connectionString", makeConnectionString(),
            "--outputHost", "localhost",
            "--outputPort", "8000"
        ), "Must specify a MarkLogic user via --outputUsername when using 'BASIC' or 'DIGEST' authentication.");
    }

    @Test
    void missingPassword() {
        assertStderrContains(() -> run(
            "copy",
            "--collections", "author",
            "--connectionString", makeConnectionString(),
            "--outputHost", "localhost",
            "--outputPort", "8000",
            "--outputUsername", "someone"
        ), "Must specify a password via --outputPassword when using 'BASIC' or 'DIGEST' authentication.");
    }

    private void assertDirectoryCount(String directoryPrefix, int expectedCount) {
        QueryManager queryManager = getDatabaseClient().newQueryManager();
        StructuredQueryDefinition query = queryManager.newStructuredQueryBuilder().directory(true, directoryPrefix);
        SearchHandle results = queryManager.search(query, new SearchHandle());
        assertEquals(expectedCount, results.getTotalResults());
    }
}
