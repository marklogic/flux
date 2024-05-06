package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class ArchivesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importArchives()
            .withConnectionString(makeConnectionString())
            .withCollections("api-archives")
            .withPath("src/test/resources/archive-files/archive.zip")
            .execute();

        assertCollectionSize("api-archives", 2);
    }
}
