package com.marklogic.newtool.command.reprocess;

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
            "--clientUri", makeClientUri(),
            "--readJavascript", "var collection; cts.uris(null, null, cts.collectionQuery(collection))",
            "--readVar", "collection=author",
            "--writeInvoke", "/writeDocument.sjs",
            "--writeVar", "theValue=my value"
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
            "--clientUri", makeClientUri(),
            "--readJavascript", "cts.uris(null, null, cts.collectionQuery('author'))",
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
            "--clientUri", makeClientUri()
        ));

        assertTrue(
            stderr.contains("Must specify one of --readInvoke, --readJavascript, --readXquery, --readJavascriptFile, or --readXqueryFile."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void missingWriteParam() {
        String stderr = runAndReturnStderr(() -> run(
            "reprocess",
            "--clientUri", makeClientUri(),
            "--readJavascript", "fn.currentDate()"
        ));

        assertTrue(
            stderr.contains("Must specify one of --writeInvoke, --writeJavascript, --writeXquery, --writeJavascriptFile, or --writeXqueryFile."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void moreThanOnePartitionParam() {
        String stderr = runAndReturnStderr(() -> run(
            "reprocess",
            "--clientUri", makeClientUri(),
            "--readJavascript", "doesn't matter",
            "--writeJavascript", "doesn't matter",
            "--readPartitionsJavascript", "doesn't matter",
            "--readPartitionsJavascriptFile", "doesn't matter"
        ));

        assertTrue(
            stderr.contains("Can only specify one of --readPartitionsInvoke, --readPartitionsJavascript, " +
                "--readPartitionsXquery, --readPartitionsJavascriptFile, or --readPartitionsXqueryFile."),
            "Unexpected stderr: " + stderr
        );
    }
}
