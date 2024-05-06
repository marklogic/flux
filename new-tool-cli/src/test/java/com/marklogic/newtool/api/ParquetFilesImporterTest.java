package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

class ParquetFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importParquetFiles()
            .withConnectionString(makeConnectionString())
            .withCollections("api-parquet")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withPath("src/test/resources/parquet/individual/cars.parquet")
            .execute();

        assertCollectionSize("api-parquet", 32);
    }

}
