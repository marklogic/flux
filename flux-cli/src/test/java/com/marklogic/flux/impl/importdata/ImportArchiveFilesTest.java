/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.PermissionsTester;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;

class ImportArchiveFilesTest extends AbstractTest {

    @Test
    void allMetadata() {
        run(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--uri-replace", ".*archive.zip,''",
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
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--categories", "collections,permissions",
            "--uri-replace", ".*archive.zip,''",
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
    void splitterSmokeTest() {
        run(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--uri-replace", ".*archive.zip,''",
            "--connection-string", makeConnectionString(),
            "--splitter-xpath", "/hello/text()"
        );

        XmlNode doc = readXmlDocument("/test/1.xml");
        doc.assertElementValue("This may not be desirable - when there's a single root element " +
                "with a text node, the chunks gets added to the root element. It's valid to do this, but it may " +
                "also be a little surprising. Though, it's likely rare to have an XML document with a single root " +
                "element and text node.",
            "/hello/chunks/chunk/text", "world");
    }

    @Test
    void dontAbortOnReadFailureByDefault() {
        String stderr = runAndReturnStderr(() -> run(
            "import-archive-files",
            "--path", "src/test/resources/archive-files",
            "--path", "src/test/resources/mlcp-archives",
            "--connection-string", makeConnectionString()
        ));

        assertFalse(stderr.contains("Command failed"),
            "The command should log error by default; stderr: " + stderr);

        assertCollectionSize("The docs from the valid MLCP archive should still be imported", "collection1", 2);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-archive-files",
            "--path", "src/test/resources/archive-files/invalid-archive.zip",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString()
        ));

        assertTrue(
            stderr.contains("Error: Could not find metadata entry for entry test/1.xml in file"),
            "Unexpected stderr: " + stderr
        );
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
