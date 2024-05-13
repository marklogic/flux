package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class JsonLinesFilesExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        NT.exportJsonLinesFiles()
            .readRows(READ_AUTHORS_OPTIC_QUERY)
            .connectionString(makeConnectionString())
            .writeFiles(tempDir.toFile().getAbsolutePath())
            .execute();

        NT.importJsonFiles()
            .readFiles(options -> options
                .paths(tempDir.toFile().getAbsolutePath())
                .jsonLines(true))
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/imported/{LastName}.json")
                .collections("imported-json-lines"))
            .execute();

        assertCollectionSize("imported-json-lines", 15);
    }
}
