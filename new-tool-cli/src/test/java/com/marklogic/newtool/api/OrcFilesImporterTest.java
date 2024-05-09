package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrcFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importOrcFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options.paths("src/test/resources/orc-files/authors.orc"))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test")
                .uriTemplate("/orc-test/{LastName}.json"))
            .execute();

        assertCollectionSize("orc-test", 15);
    }

    @Test
    void badOption() {
        OrcFilesImporter importer = NT.importOrcFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/orc-files/authors.orc")
                .additionalOptions(Map.of("mergeSchema", "not-valid-value"))
            )
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("orc-test"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> importer.execute());
        assertEquals("For input string: \"not-valid-value\"", ex.getMessage(), "Expecting a failure due to the " +
            "invalid value for the ORC 'mergeSchema' option.");
    }
}
