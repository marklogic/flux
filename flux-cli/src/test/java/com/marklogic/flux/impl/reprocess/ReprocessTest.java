/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.reprocess;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import static org.junit.jupiter.api.Assertions.*;

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
            "--write-var", "theValue=my value",

            // Included to ensure it doesn't cause any failures. This is only done for performance reasons, and we
            // don't have any assertions to make on the effect of using multiple threads/partitions.
            "--thread-count", "8"
        );

        // reprocess-test is the collection used by writeDocument.sjs.
        assertCollectionSize("reprocess-test", 15);

        for (int i = 1; i <= 15; i++) {
            String uri = String.format("/reprocess-test/author/author%d.json", i);
            JsonNode doc = readJsonDocument(uri);
            assertEquals("my value", doc.get("theValue").asText());
        }
    }

    /**
     * When running this test with Java 17, we get this error without the appropriate "--add-opens":
     * <p>
     * SerializationDebugger$ (in unnamed module @0x543c6f6d) cannot access class sun.security.action.GetBooleanAction (in module java.base)
     * <p>
     * And the test does not finish, Spark just seems to hang. So this test only runs on Java 11. Note that the
     * build.gradle file includes the necessary "--add-opens" to avoid this scenario.
     */
    @Test
    @EnabledOnJre({JRE.JAVA_11})
    void badJavascript() {
        String stderr = runAndReturnStderr(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "throw Error('Boom!')",
            "--write-invoke", "/writeDocument.sjs"
        );

        assertTrue(stderr.contains("Internal Server Error. Server Message: JS-JAVASCRIPT: throw Error('Boom!')"),
            "Unexpected stderr: " + stderr);

        assertFalse(stderr.contains("at com.marklogic"),
            "The Main program should attempt to remove the stacktrace that Spark oddly includes in the " +
                "exception message. A user that wants to see the stacktrace can use --stacktrace to do so. " +
                "The presence of 'at com.marklogic' indicates that a stacktrace is present. " +
                "Actual stderr: " + stderr);
    }

    @Test
    void processBatchOfItems() {
        run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "var collection; cts.uris(null, null, cts.collectionQuery('author'))",
            "--write-invoke", "/writeBatch.sjs",
            "--batch-size", "10"
        );

        // reprocess-batch-test is used by writeBatch.sjs.
        assertCollectionSize(
            "writeBatch.sjs is expected to receive a comma-delimited list of URIs. This approach typically offers " +
                "much better performance as it allows for the writer module to handle multiple items in a single call " +
                "to MarkLogic as opposed to making one call per item.",
            "reprocess-batch-test", 15);
    }

    /**
     * This is used primarily for manual inspection of the progress messages. We don't yet have a reliable way of
     * asserting on log messages, so using manual inspection for now.
     */
    @Test
    void logProgressTest() {
        run(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-xquery", "for $i in 1 to 100 return $i",
            "--log-read-progress", "10",
            "--write-invoke", "/writeDocument.sjs",
            "--write-var", "theValue=my value",
            "--log-progress", "10"
        );

        assertCollectionSize("reprocess-test", 100);
    }

    @Test
    void missingReadParam() {
        String stderr = runAndReturnStderr(
            "reprocess",
            "--connection-string", makeConnectionString()
        );

        assertTrue(
            stderr.contains("Must specify one of --read-invoke, --read-javascript, --read-xquery, --read-javascript-file, or --read-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void missingWriteParam() {
        String stderr = runAndReturnStderr(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "fn.currentDate()"
        );

        assertTrue(
            stderr.contains("Must specify one of --write-invoke, --write-javascript, --write-xquery, --write-javascript-file, or --write-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }

    @Test
    void moreThanOnePartitionParam() {
        String stderr = runAndReturnStderr(
            "reprocess",
            "--connection-string", makeConnectionString(),
            "--read-javascript", "doesn't matter",
            "--write-javascript", "doesn't matter",
            "--read-partitions-javascript", "doesn't matter",
            "--read-partitions-javascript-file", "doesn't matter"
        );

        assertTrue(
            stderr.contains("Can only specify one of --read-partitions-invoke, --read-partitions-javascript, " +
                "--read-partitions-xquery, --read-partitions-javascript-file, or --read-partitions-xquery-file."),
            "Unexpected stderr: " + stderr
        );
    }
}
