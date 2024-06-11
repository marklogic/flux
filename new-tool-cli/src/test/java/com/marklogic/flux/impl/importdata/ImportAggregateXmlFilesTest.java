package com.marklogic.flux.impl.importdata;

import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAggregateXmlFilesTest extends AbstractTest {

    @Test
    void elementIsRequired() {
        String stderr = runAndReturnStderr(() -> run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--connectionString", makeConnectionString()
        ));
        assertTrue(stderr.contains("The following option is required: [--element]"),
            "Unexpected stderr: " + stderr);
    }

    @Test
    void withElement() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "withElement-test",
            "--uriReplace", ".*/xml-file,''"
        );

        assertCollectionSize("withElement-test", 3);
        verifyDoc("/people.xml-1.xml", "Person-1", "company-1", "/person/%s");
        verifyDoc("/people.xml-2.xml", "Person-2", "company-2", "/person/%s");
        verifyDoc("/people.xml-3.xml", "Person-3", "company-3", "/person/%s");
    }

    @Test
    void withElementAndNamespace() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people-with-namespace.xml",
            "--element", "person",
            "--namespace", "org:example",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "withElementAndNamespace-test",
            "--uriReplace", ".*/xml-file,''"
        );

        assertCollectionSize("withElementAndNamespace-test", 3);
        verifyDoc("/people-with-namespace.xml-1.xml", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("/people-with-namespace.xml-2.xml", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("/people-with-namespace.xml-3.xml", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void withAllOptions() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people-with-namespace.xml",
            "--element", "person",
            "--namespace", "org:example",
            "--uriElement", "name",
            "--uriNamespace", "org:example",
            "--connectionString", makeConnectionString(),
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
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importZippedXml-test",
            "--compression", "zip",
            "--element", "",
            "--uriReplace", ".*/single-xml.zip,''"
        );
        assertCollectionSize("importZippedXml-test", 1);
    }

    @Test
    void importZippedXmlWithElement() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/single-xml.zip",
            "--element", "person",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importZippedXmlWithElement-test",
            "--uriElement", "name",
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
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/multiple-xmls.zip",
            "--element", "",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importMultipleZippedXml-test",
            "--compression", "zip",
            "--uriReplace", ".*/temp,''"
        );
        assertCollectionSize("importMultipleZippedXml-test", 3);
        verifyDocContents("/hello.xml", "src/test/resources/xml-file/temp/hello.xml");
        verifyDocContents("/people.xml", "src/test/resources/xml-file/temp/people.xml");
        verifyDocContents("/people-with-namespace.xml", "src/test/resources/xml-file/temp/people-with-namespace.xml");
    }

    @Test
    void importGzippedXml() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml.gz",
            "--element", "person",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "importGzippedXml-test",
            "--compression", "gzip",
            "--uriElement", "name"
        );

        assertCollectionSize("importGzippedXml-test", 3);
        verifyDoc("Person-1", "Person-1", "company-1", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-2", "Person-2", "company-2", "/*[name()='person']/*[name()='%s']");
        verifyDoc("Person-3", "Person-3", "company-3", "/*[name()='person']/*[name()='%s']");
    }

    @Test
    void dontAbortOnReadFailureByDefault() {
        String stderr = runAndReturnStderr(() -> run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--connectionString", makeConnectionString(),
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
            "import_aggregate_xml_files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--abortOnReadFailure",
            "--element", "person",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "agg-xml"
        ));

        assertTrue(stderr.contains("Command failed, cause: Unable to read XML from file"),
            "With --abortOnReadFailure included, the command should fail if it cannot read a file; stderr: " + stderr);
        assertCollectionSize("agg-xml", 0);
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
