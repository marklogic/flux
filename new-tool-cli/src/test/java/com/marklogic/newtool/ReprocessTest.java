package com.marklogic.newtool;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReprocessTest extends AbstractTest {

    @Test
    void javascript() {
        run(
            "reprocess",
            "--read-javascript", "cts.uris(null, [], cts.trueQuery())",
            "--write-javascript", "var URI; console.log('URI', URI);"
        );
    }

    @Test
    void readAndWriteVars() {
        run(
            "reprocess",
            "--read-javascript", "Sequence.from([VAR1, VAR2])",
            "-RV:VAR1=hey",
            "-RV:VAR2=you",
            "--write-invoke", "/writeDocs.sjs",
            "-WV:THE_VALUE=the-value"
        );

        Stream.of("/test/hey.json", "/test/you.json").forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertEquals("the-value", doc.get("hello").asText());
        });
    }
}
