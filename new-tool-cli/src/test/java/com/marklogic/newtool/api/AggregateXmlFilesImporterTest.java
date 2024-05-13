package com.marklogic.newtool.api;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class AggregateXmlFilesImporterTest extends AbstractTest {

    @Test
    void uriElementAndNamespace() {
        NT.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/xml-file/people-with-namespace.xml")
                .element("person")
                .namespace("org:example")
                .uriElement("name")
                .uriNamespace("org:example"))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("with-all-options")
                .uriSuffix(".xml"))
            .execute();

        assertCollectionSize("with-all-options", 3);
        Stream.of("Person-1.xml", "Person-2.xml", "Person-3.xml").forEach(uri -> {
            XmlNode doc = readXmlDocument(uri);
            doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example")});
            doc.assertElementExists("/ex:person/ex:name");
        });
    }

    @Test
    void zip() {
        NT.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .readFiles(options -> options
                .paths("src/test/resources/xml-file/single-xml.zip")
                .compressionType(CompressionType.ZIP)
                .element("person"))
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("zipped-person"))
            .execute();

        assertCollectionSize("zipped-person", 3);
    }
}