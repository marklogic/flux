package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class MlcpArchivesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importMlcpArchives()
            .withConnectionString(makeConnectionString())
            .withPath("src/test/resources/mlcp-archives")
            .execute();

        assertCollectionSize("collection1", 2);
        assertCollectionSize("collection2", 2);
    }
}
