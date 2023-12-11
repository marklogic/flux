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
            "--readJavascript", "cts.uris(null, [], cts.trueQuery())",
            "--writeJavascript", "var URI; console.log('URI', URI);"
        );
    }

    @Test
    void readAndWriteVars() {
        run(
            "reprocess",
            "--readJavascript", "Sequence.from([VAR1, VAR2])",
            "-RV:VAR1=hey",
            "-RV:VAR2=you",
            "--writeInvoke", "/writeDocs.sjs",
            "-WV:THE_VALUE=the-value"
        );

        Stream.of("/test/hey.json", "/test/you.json").forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertEquals("the-value", doc.get("hello").asText());
        });
    }
}
