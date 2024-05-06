package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class DelimitedFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importDelimitedFiles()
            .withConnectionString(makeConnectionString())
            .withCollections("api-csv")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withPath("src/test/resources/delimited-files/three-rows.csv")
            .execute();

        assertCollectionSize("api-csv", 3);
    }

}
