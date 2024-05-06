package com.marklogic.newtool.api;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

class AggregateXmlFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        NT.importAggregateXmlFiles()
            .withConnectionString(makeConnectionString())
            .withPath("src/test/resources/xml-file/people-with-namespace.xml")
            .withCollections("xml-files")
            .withPermissionsString(DEFAULT_PERMISSIONS)
            .withElement("person")
            .withNamespace("org:example")
            .execute();

        getUrisInCollection("xml-files", 3).forEach(uri -> {
            XmlNode doc = readXmlDocument(uri);
            doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example")});
            doc.assertElementExists("/ex:person/ex:name");
        });
    }
}
