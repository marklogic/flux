/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.PermissionsTester;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportMlcpArchiveFilesTest extends AbstractTest {

    @Test
    void allMetadata() {
        run(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/mlcp-archives",
            "--connection-string", makeConnectionString(),

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "2"
        );

        for (String uri : getUrisInCollection("collection1", 2)) {
            XmlNode doc = readXmlDocument(uri);
            assertEquals("world", doc.getElementValue("/hello"));

            DocumentMetadataHandle metadata = getDatabaseClient().newDocumentManager().readMetadata(uri, new DocumentMetadataHandle());
            assertEquals(10, metadata.getQuality());
            verifyCollections(metadata);
            verifyPermissions(metadata);

            assertEquals("value1", metadata.getMetadataValues().get("meta1"));
            assertEquals("value2", metadata.getMetadataValues().get("meta2"));
            assertEquals("value1", metadata.getProperties().get(new QName("org:example", "key1")));
            assertEquals("value2", metadata.getProperties().get("key2"));
        }
    }

    @Test
    void subsetOfMetadata() {
        run(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/mlcp-archives",
            "--categories", "collections,permissions",
            "--connection-string", makeConnectionString()
        );

        for (String uri : getUrisInCollection("collection1", 2)) {
            XmlNode doc = readXmlDocument(uri);
            assertEquals("world", doc.getElementValue("/hello"));

            DocumentMetadataHandle metadata = getDatabaseClient().newDocumentManager().readMetadata(uri, new DocumentMetadataHandle());
            verifyCollections(metadata);
            verifyPermissions(metadata);

            assertEquals(0, metadata.getMetadataValues().size());
            assertEquals(0, metadata.getProperties().size());
            assertEquals(0, metadata.getQuality());
        }
    }

    @Test
    void invalidFileDontAbort() {
        run(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/mlcp-archives",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--connection-string", makeConnectionString()
        );

        assertCollectionSize("The error from the non-MLCP-archive file goodbye.zip should have been logged " +
            "and should not have caused the command to fail. And so the two documents in the valid MLCP archive " +
            "should still have been imported into 'collection1'.", "collection1", 2);
    }

    @Test
    void invalidFileAbort() {
        String stderr = runAndReturnStderr(
            "import-mlcp-archive-files",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString()
        );

        assertTrue(stderr.contains("Error: Unable to read metadata for entry: goodbye.json"),
            "Unexpected stderr: " + stderr);
    }

    private void verifyCollections(DocumentMetadataHandle metadata) {
        assertEquals(2, metadata.getCollections().size());
        assertTrue(metadata.getCollections().contains("collection1"));
        assertTrue(metadata.getCollections().contains("collection2"));
    }

    private void verifyPermissions(DocumentMetadataHandle metadata) {
        PermissionsTester tester = new PermissionsTester(metadata.getPermissions());
        tester.assertReadPermissionExists("spark-user-role");
        tester.assertUpdatePermissionExists("spark-user-role");
        tester.assertReadPermissionExists("qconsole-user");
    }
}
