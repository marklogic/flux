package com.marklogic.newtool.command;

import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportXmlAggregateTest extends AbstractTest {

    @Test
    void shouldThrowExceptionWithoutElement() {
        try{
            run(
                "import_aggregate_xml_files",
                "--path", "src/test/resources/xml-file/people.xml",
                "--clientUri", makeClientUri(),
                "--permissions", DEFAULT_PERMISSIONS,
                "--collections", "xml-test",
                "--uriTemplate", "/xml/test.txt"
            );
        } catch(Exception ex) {
            assertEquals("The following option is required: [--element]", ex.getMessage(),
                "Should throw an exception since element parameter is required.");
        }
    }

    @Test
    void withElement() {
        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
            "--clientUri", makeClientUri(),
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
