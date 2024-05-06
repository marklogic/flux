package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class OrcFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importOrcFiles()
            .withConnectionString(makeConnectionString())
            .withCollections("api-orc")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withPath("src/test/resources/orc-files/authors.orc")
            .execute();

        assertCollectionSize("api-orc", 15);
    }

}
