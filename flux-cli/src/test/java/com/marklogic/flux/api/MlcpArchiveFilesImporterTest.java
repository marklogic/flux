package com.marklogic.flux.api;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MlcpArchiveFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importMlcpArchiveFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/mlcp-archives")
                .categories("content", "permissions"))
            .execute();

        Stream.of("/test/1.xml", "/test/2.xml").forEach(uri -> {
            DocumentMetadataHandle metadata = getDatabaseClient().newDocumentManager().readMetadata(uri, new DocumentMetadataHandle());
            assertEquals(0, metadata.getCollections().size(), "Since 'collections' was not included in the list " +
                "of categories, no collections should be assigned to URI: " + uri);
        });
    }

    @Test
    void missingPath() {
        MlcpArchiveFilesImporter importer = Flux.importMlcpArchiveFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
