/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReprocessorTest extends AbstractTest {

    @Test
    void test() {
        Flux.reprocess()
            .connectionString(makeConnectionString())
            .from(options -> options
                .javascript("var collection; cts.uris(null, null, cts.collectionQuery(collection))")
                .vars(Map.of("collection", "author")))

            // Has no functional impact, just ensuring it doesn't cause a break.
            .repartition(32)

            .to(options -> options
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
        Reprocessor r = Flux.reprocess()
            .connectionString(makeConnectionString())
            .to(options -> options.invoke("/writeDocument.sjs"));

        FluxException ex = assertThrowsFluxException(r::execute);
        assertEquals("Must specify either JavaScript code, XQuery code, or an invokable module for reading from MarkLogic", ex.getMessage());
    }

    @Test
    void twoPartitionReaders() {
        Reprocessor r = Flux.reprocess()
            .connectionString(makeConnectionString())
            .from(options -> options.invoke("anything.sjs")
                .partitionsJavascript("something")
                .partitionsXquery("something else"))
            .to(options -> options.invoke("/writeDocument.sjs"));

        FluxException ex = assertThrowsFluxException(r::execute);
        assertEquals("Can only specify one approach for defining partitions that are sent to the code for reading from MarkLogic", ex.getMessage());
    }

    @Test
    void noWriter() {
        Reprocessor r = Flux.reprocess()
            .connectionString(makeConnectionString())
            .from(options -> options.invoke("anything"));

        FluxException ex = assertThrowsFluxException(r::execute);
        assertEquals("Must specify either JavaScript code, XQuery code, or an invokable module for writing to MarkLogic", ex.getMessage());
    }
}
