package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importJsonFiles()
            .withConnectionString(makeConnectionString())
            .withCollections("api-json")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withJsonLines(true)
            .withJsonRootName("rootTest")
            .withPath("src/test/resources/delimited-files/line-delimited-json.txt")
            .execute();

        getUrisInCollection("api-json", 3).forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertTrue(doc.has("rootTest"));
            assertTrue(doc.get("rootTest").has("firstName"));
        });
    }

}
