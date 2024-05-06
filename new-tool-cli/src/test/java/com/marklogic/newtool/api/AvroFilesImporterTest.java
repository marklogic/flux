package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class AvroFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importAvroFiles()
            .withConnectionString(makeConnectionString())
            .withCollections("api-avro")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withPath("src/test/resources/avro/colors.avro")
            .execute();

        assertCollectionSize("api-avro", 3);
    }
}
