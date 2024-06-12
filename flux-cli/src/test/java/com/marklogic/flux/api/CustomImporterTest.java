package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CustomImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.customImport()
            .from(options -> options
                .source("csv")
                .additionalOptions(Map.of(
                    "path", "src/test/resources/delimited-files/semicolon-delimiter.csv",
                    "delimiter", ";",
                    "header", "true"
                )))
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("csv-test")
                .uriTemplate("/csv/{number}.json"))
            .execute();

        assertCollectionSize("csv-test", 3);
    }
}
