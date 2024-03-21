package com.marklogic.newtool.command;

import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyTest extends AbstractTest {

    @Test
    void sameDatabase() {
        run(
            "copy",
            "--categories", "content",
            "--collections", "author",
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
            "--outputClientUri", makeClientUri(),
            // No need to specify permissions since they are included via "--categories".
            "--outputCollections", "author-copies",
            "--outputUriPrefix", "/copied"
        );

        assertCollectionSize("author", 15);
        assertCollectionSize("author-copies", 15);
        assertDirectoryCount("/copied/", 15);
    }

    private void assertDirectoryCount(String directoryPrefix, int expectedCount) {
        QueryManager queryManager = getDatabaseClient().newQueryManager();
        StructuredQueryDefinition query = queryManager.newStructuredQueryBuilder().directory(true, directoryPrefix);
        SearchHandle results = queryManager.search(query, new SearchHandle());
        assertEquals(expectedCount, results.getTotalResults());
    }
}
