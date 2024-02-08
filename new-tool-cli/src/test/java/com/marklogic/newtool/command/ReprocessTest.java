package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
