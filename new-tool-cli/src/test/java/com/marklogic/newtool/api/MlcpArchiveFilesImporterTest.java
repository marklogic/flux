package com.marklogic.newtool.api;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MlcpArchiveFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importMlcpArchiveFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/mlcp-archives")
                .categories("content", "permissions"))
            .execute();

        Stream.of("/test/1.xml", "/test/2.xml").forEach(uri -> {
            DocumentMetadataHandle metadata = getDatabaseClient().newDocumentManager().readMetadata(uri, new DocumentMetadataHandle());
            assertEquals(0, metadata.getCollections().size(), "Since 'collections' was not included in the list " +
                "of categories, no collections should be assigned to URI: " + uri);
        });
    }
}
