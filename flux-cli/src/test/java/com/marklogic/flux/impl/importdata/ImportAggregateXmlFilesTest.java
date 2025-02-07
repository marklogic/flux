/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAggregateXmlFilesTest extends AbstractTest {

    @Test
    void elementIsRequired() {
        assertStderrContains(() -> run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--connection-string", makeConnectionString()
        ), "Missing required option: '--element <element>'");
    }

    @Test
    void elementAndPathAreRequired() {
        assertStderrContains(() -> run(
            "import-aggregate-xml-files",
            "--connection-string", makeConnectionString()
        ), "Missing required options: '--element <element>', '--path <path>'");
    }

    @Test
    void withElement() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "withElement-test",
            "--uri-replace", ".*/xml-file,''",

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "2"
        );

        assertCollectionSize("withElement-test", 3);
        verifyDoc("/people.xml-1.xml", "Person-1", "company-1", "/person/%s");
        verifyDoc("/people.xml-2.xml", "Person-2", "company-2", "/person/%s");
        verifyDoc("/people.xml-3.xml", "Person-3", "company-3", "/person/%s");
    }

    @Test
    void withElementAndNamespace() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people-with-namespace.xml",
            "--element", "person",
            "--namespace", "org:example",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "withElementAndNamespace-test",
            "--uri-replace", ".*/xml-file,''"
        );

        assertCollectionSize("withElementAndNamespace-test", 3);
        verifyDoc("/people-with-namespace.xml-1.xml", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("/people-with-namespace.xml-2.xml", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("/people-with-namespace.xml-3.xml", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void withAllOptions() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people-with-namespace.xml",
            "--element", "person",
            "--namespace", "org:example",
            "--uri-element", "name",
            "--uri-namespace", "org:example",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "withAllOptions-test"
        );

        assertCollectionSize("withAllOptions-test", 3);
        verifyDoc("Person-1", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-2", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-3", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void importZippedXml() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importZippedXml-test",
            "--compression", "zip",
            "--element", "",
            "--uri-replace", ".*/single-xml.zip,''"
        );
        assertCollectionSize("importZippedXml-test", 1);
    }

    @Test
    void importZippedXmlWithElement() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importZippedXmlWithElement-test",
            "--uri-element", "name",
            "--compression", "zip"
        );
        assertCollectionSize("importZippedXmlWithElement-test", 3);
        verifyDoc("Person-1", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-2", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-3", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void importMultipleZippedXml() throws FileNotFoundException {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/multiple-xmls.zip",
            "--element", "",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importMultipleZippedXml-test",
            "--compression", "zip",
            "--uri-replace", ".*/temp,''"
        );
        assertCollectionSize("importMultipleZippedXml-test", 3);
        verifyDocContents("/hello.xml", "src/test/resources/xml-file/temp/hello.xml");
        verifyDocContents("/people.xml", "src/test/resources/xml-file/temp/people.xml");
        verifyDocContents("/people-with-namespace.xml", "src/test/resources/xml-file/temp/people-with-namespace.xml");
    }

    @Test
    void importGzippedXml() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people.xml.gz",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importGzippedXml-test",
            "--compression", "gzip",
            "--uri-element", "name"
        );

        assertCollectionSize("importGzippedXml-test", 3);
        verifyDoc("Person-1", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-2", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-3", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void dontAbortOnReadFailureByDefault() {
        String stderr = runAndReturnStderr(() -> run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "agg-xml"
        ));

        assertFalse(stderr.contains("Command failed"),
            "The command should default to logging read failures and not having the command fail; actual stderr: " + stderr);
        assertCollectionSize("agg-xml", 3);
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--abort-on-read-failure",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "agg-xml"
        ));

        assertTrue(stderr.contains("Error: Unable to read XML from file"),
            "With --abort-on-read-failure included, the command should fail if it cannot read a file; stderr: " + stderr);
        assertCollectionSize("agg-xml", 0);
    }

    /**
     * This demonstrates that this command can import an XML document as-is.
     */
    @Test
    void topLevelElement() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "people",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "top-level-test",
            "--uri-replace", ".*/xml-file,''"
        );

        assertCollectionSize("top-level-test", 1);
    }

    @Test
    void withEncoding() {
        run(
            "import-aggregate-xml-files",
            "--path", "src/test/resources/encoding/medline.iso-8859-1.txt",
            "--element", "MedlineCitation",
            "--encoding", "ISO-8859-1",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "encoding-test",
            "--uri-element", "PMID",
            "--uri-suffix", ".xml"
        );

        assertCollectionSize("encoding-test", 2);
        readXmlDocument("10605436.xml").assertElementExists("/MedlineCitation");
        readXmlDocument("12261559.xml").assertElementExists("/MedlineCitation");
    }

    private void verifyDoc(String uri, String name, String company, String xpath) {
        XmlNode doc = readXmlDocument(uri);
        doc.assertElementValue(String.format(xpath, "name"), name);
        doc.assertElementValue(String.format(xpath, "company"), company);
    }

    private void verifyDocContents(String uri, String localUri) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(localUri)).useDelimiter("\\Z");
        String readContent = scanner.next();
        scanner.close();

        XMLDocumentManager documentManager = getDatabaseClient().newXMLDocumentManager();
        String docContent = documentManager.read(uri).next().getContent(new StringHandle()).toString();
        assertTrue(docContent.contains(readContent));
    }
}
