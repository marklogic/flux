package com.marklogic.newtool.api;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.junit5.PermissionsTester;
import com.marklogic.newtool.AbstractTest;
import org.apache.spark.sql.AnalysisException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericFilesImporterTest extends AbstractTest {

    private static final String PATH = "src/test/resources/mixed-files/hello*";

    @Test
    void test() {
        NT.importGenericFiles()
            .withConnectionString(makeConnectionString())
            .withPath(PATH)
            .withCollectionsString("api-files,second-collection")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .execute();

        assertCollectionSize("api-files", 4);
        assertCollectionSize("second-collection", 4);
    }

    @Test
    void withPermissionMap() {
        NT.importGenericFiles()
            .withConnectionString(makeConnectionString())
            .withPath(PATH)
            .withCollections("mixed-files", "another-test")
            .withPermissions(Map.of(
                "new-tool-role", Set.of(DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
                "qconsole-user", Set.of(DocumentMetadataHandle.Capability.READ)
            ))
            .execute();

        assertCollectionSize("mixed-files", 4);
        getUrisInCollection("another-test", 4).forEach(uri -> {
            PermissionsTester tester = readDocumentPermissions(uri);
            tester.assertReadPermissionExists("new-tool-role");
            tester.assertUpdatePermissionExists("new-tool-role");
            tester.assertReadPermissionExists("qconsole-user");
        });
    }

    @Test
    void badPath() {
        GenericFilesImporter command = NT.importGenericFiles()
            .withConnectionString(makeConnectionString())
            .withPath("path/doesnt/exist");

        AnalysisException ex = assertThrows(AnalysisException.class, () -> command.execute());
        assertTrue(ex.getMessage().contains("Path does not exist"),
            "Unexpected message: " + ex.getMessage() + ". And I'm not sure we want this Spark-specific " +
                "exception to escape. Think we need for AbstractCommand to catch Throwable and look for a " +
                "Spark-specific exception. If one is found, we need a new generic 'NT' runtime exception to throw " +
                "with the Spark-specific exception message.");
    }
}
