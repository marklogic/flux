/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

/**
 * Has tests for each command that reads arbitrary rows and thus supports converting those into XML documents.
 */
class ImportRowsAsXmlTest extends AbstractTest {

    @Test
    void delimitedText() {
        run(
            "import-delimited-files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-xml",
            "--uri-template", "/delimited/{number}.xml",
            "--xml-root-name", "myDelimitedText",
            "--xml-namespace", "csv.org"
        );

        assertCollectionSize("delimited-xml", 3);
        XmlNode doc = readXmlDocument("/delimited/1.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("c", "csv.org")});
        doc.assertElementValue("/c:myDelimitedText/c:color", "blue");
    }

    @Test
    void jdbc() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.xml",
            "--collections", "jdbc-customer",
            "--xml-root-name", "CUSTOMER",
            "--xml-namespace", "org:example"
        );

        assertCollectionSize("jdbc-customer", 10);
        XmlNode doc = readXmlDocument("/customer/1.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("oe", "org:example")});
        doc.assertElementValue("/oe:CUSTOMER/oe:store_id", "1");
    }

    @Test
    void parquet() {
        run(
            "import-parquet-files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--xml-root-name", "myParquet",
            "--xml-namespace", "parquet.org",
            "--uri-template", "/parquet/{model}.xml"
        );

        assertCollectionSize("parquet-test", 32);
        XmlNode doc = readXmlDocument("/parquet/AMC Javelin.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("p", "parquet.org")});
        doc.assertElementValue("/p:myParquet/p:model", "AMC Javelin");
    }

    @Test
    void avro() {
        run(
            "import-avro-files",
            "--path", "src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "--uri-template", "/avro/{color}.xml",
            "--xml-root-name", "myAvro",
            "--xml-namespace", "avro.org"
        );

        assertCollectionSize("avro-test", 6);
        XmlNode doc = readXmlDocument("/avro/blue.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("a", "avro.org")});
        doc.assertElementValue("/a:myAvro/a:number", "1");
    }

    @Test
    void orc() {
        run(
            "import-orc-files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-test",
            "--uri-template", "/orc/{ForeName}",
            "--xml-root-name", "myOrc",
            "--xml-namespace", "orc.org"
        );

        assertCollectionSize("orc-test", 15);
        XmlNode doc = readXmlDocument("/orc/Appolonia");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("o", "orc.org")});
        doc.assertElementValue("/o:myOrc/o:LastName", "Edeler");
    }
}
