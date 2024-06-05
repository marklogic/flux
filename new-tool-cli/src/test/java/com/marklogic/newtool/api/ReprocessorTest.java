package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReprocessorTest extends AbstractTest {

    @Test
    void test() {
        NT.reprocess()
            .connectionString(makeConnectionString())
            .readItems(options -> options
                .javascript("var collection; cts.uris(null, null, cts.collectionQuery(collection))")
                .vars(Map.of("collection", "author")))
            .writeItems(options -> options
                .invoke("/writeDocument.sjs")
                .vars(Map.of("theValue", "my value")))
            .execute();

        assertCollectionSize("reprocess-test", 15);
        for (int i = 1; i <= 15; i++) {
            String uri = String.format("/reprocess-test/author/author%d.json", i);
            JsonNode doc = readJsonDocument(uri);
            assertEquals("my value", doc.get("theValue").asText());
        }
    }

    @Test
    void noReader() {
        Reprocessor r = NT.reprocess()
            .connectionString(makeConnectionString())
            .writeItems(options -> options.invoke("/writeDocument.sjs"));

        NtException ex = assertThrowsNtException(() -> r.execute());
        assertEquals("Must specify either JavaScript code, XQuery code, or an invokable module for reading from MarkLogic", ex.getMessage());
    }

    @Test
    void twoPartitionReaders() {
        Reprocessor r = NT.reprocess()
            .connectionString(makeConnectionString())
            .readItems(options -> options.invoke("anything.sjs")
                .partitionsJavascript("something")
                .partitionsXquery("something else"))
            .writeItems(options -> options.invoke("/writeDocument.sjs"));

        NtException ex = assertThrowsNtException(() -> r.execute());
        assertEquals("Can only specify one approach for defining partitions that are sent to the code for reading from MarkLogic", ex.getMessage());
    }

    @Test
    void noWriter() {
        Reprocessor r = NT.reprocess()
            .connectionString(makeConnectionString())
            .readItems(options -> options.invoke("anything"));

        NtException ex = assertThrowsNtException(() -> r.execute());
        assertEquals("Must specify either JavaScript code, XQuery code, or an invokable module for writing to MarkLogic", ex.getMessage());
    }
}
