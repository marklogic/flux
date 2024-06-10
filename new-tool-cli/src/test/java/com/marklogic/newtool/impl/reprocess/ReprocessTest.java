package com.marklogic.newtool.impl.reprocess;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Just verifies one of the many possible combinations for reprocessing. ReprocessOptionsTest is used to ensure that
 * all args are set correctly as options, and then we expect our connector to work properly based on those options.
 */
class ReprocessTest extends AbstractTest {

    @Test
    void test() {
        run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "var collection; cts.uris(null, null, cts.collectionQuery(collection))",
            "--read-var", "collection=author",
            "--write-invoke", "/writeDocument.sjs",
            "--write-var", "theValue=my value"
        );

        // reprocess-test is the collection used by writeDocument.sjs.
        assertCollectionSize("reprocess-test", 15);

        for (int i = 1; i <= 15; i++) {
            String uri = String.format("/reprocess-test/author/author%d.json", i);
            JsonNode doc = readJsonDocument(uri);
            assertEquals("my value", doc.get("theValue").asText());
        }
    }

    @Test
    void previewDoesntRequireWriteParam() {
        String stdout = runAndReturnStdout(() -> run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "cts.uris(null, null, cts.collectionQuery('author'))",
            "--preview", "2"
        ));

        assertTrue(stdout.contains("only showing top 2 rows"),
            "No 'write' param should be required when a user uses '--preview', as the user is specifically asking " +
                "just to see the read data and not to write anything.");
    }

    @Test
    void missingReadParam() {
        String stderr = runAndReturnStderr(() -> run(
            "reprocess",
            "--connection-string", makeConnectionString()
        ));

        assertTrue(
            stderr.contains("Must specify one of --read-invoke, --read-javascript, --read-xquery, --read-javascript-file, or --read-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void missingWriteParam() {
        String stderr = runAndReturnStderr(() -> run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "fn.currentDate()"
        ));

        assertTrue(
            stderr.contains("Must specify one of --write-invoke, --write-javascript, --write-xquery, --write-javascript-file, or --write-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void moreThanOnePartitionParam() {
        String stderr = runAndReturnStderr(() -> run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "doesn't matter",
            "--write-javascript", "doesn't matter",
            "--read-partitions-javascript", "doesn't matter",
            "--read-partitions-javascript-file", "doesn't matter"
        ));

        assertTrue(
            stderr.contains("Can only specify one of --read-partitions-invoke, --read-partitions-javascript, " +
                "--read-partitions-xquery, --read-partitions-javascript-file, or --read-partitions-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }
}
