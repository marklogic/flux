package com.marklogic.newtool.command;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportXmlAggregateTest extends AbstractTest {

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
            assertEquals(ex.getMessage(), "The following option is required: [--element]",
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

    private void verifyDoc(String uri, String name, String company, String xpath) {
        XmlNode doc = readXmlDocument(uri);
        doc.assertElementValue(String.format(xpath, "name"), name);
        doc.assertElementValue(String.format(xpath, "company"), company);
    }
}
