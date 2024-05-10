package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CustomImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.customImport()
            .readData(options -> options
                .source("csv")
                .additionalOptions(Map.of(
                    "path", "src/test/resources/delimited-files/semicolon-delimiter.csv",
                    "delimiter", ";",
                    "header", "true"
                )))
            .connectionString(makeConnectionString())
            .writeData(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("csv-test")
                .uriTemplate("/csv/{number}.json"))
            .execute();

        assertCollectionSize("csv-test", 3);
    }
}
