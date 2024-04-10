package com.marklogic.newtool.command;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

/**
 * Has tests for each command that reads arbitrary rows and thus supports converting those into XML documents.
 */
class ImportRowsAsXmlTest extends AbstractTest {

    @Test
    void delimitedText() {
        run(
            "import_delimited_files",
            "--path", "src/test/resources/delimited-files/three-rows.csv",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "delimited-xml",
            "--uriTemplate", "/delimited/{number}.xml",
            "--xmlRootName", "myDelimitedText",
            "--xmlNamespace", "csv.org"
        );

        assertCollectionSize("delimited-xml", 3);
        XmlNode doc = readXmlDocument("/delimited/1.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("c", "csv.org")});
        doc.assertElementValue("/c:myDelimitedText/c:color", "blue");
    }

    @Test
    void jdbc() {
        run(
            "import_jdbc",
            "--jdbcUrl", PostgresUtil.URL,
            "--jdbcUser", PostgresUtil.USER,
            "--jdbcPassword", PostgresUtil.PASSWORD,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uriTemplate", "/customer/{customer_id}.xml",
            "--collections", "jdbc-customer",
            "--xmlRootName", "CUSTOMER",
            "--xmlNamespace", "org:example"
        );

        assertCollectionSize("jdbc-customer", 10);
        XmlNode doc = readXmlDocument("/customer/1.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("oe", "org:example")});
        doc.assertElementValue("/oe:CUSTOMER/oe:store_id", "1");
    }

    @Test
    void parquet() {
        run(
            "import_parquet_files",
            "--path", "src/test/resources/parquet/individual/cars.parquet",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--xmlRootName", "myParquet",
            "--xmlNamespace", "parquet.org",
            "--uriTemplate", "/parquet/{model}.xml"
        );

        assertCollectionSize("parquet-test", 32);
        XmlNode doc = readXmlDocument("/parquet/AMC Javelin.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("p", "parquet.org")});
        doc.assertElementValue("/p:myParquet/p:model", "AMC Javelin");
    }

    @Test
    void avro() {
        run(
            "import_avro_files",
            "--path", "src/test/resources/avro/*",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "--uriTemplate", "/avro/{color}.xml",
            "--xmlRootName", "myAvro",
            "--xmlNamespace", "avro.org"
        );

        assertCollectionSize("avro-test", 6);
        XmlNode doc = readXmlDocument("/avro/blue.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("a", "avro.org")});
        doc.assertElementValue("/a:myAvro/a:number", "1");
    }

    @Test
    void orc() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orc-test",
            "--uriTemplate", "/orc/{ForeName}",
            "--xmlRootName", "myOrc",
            "--xmlNamespace", "orc.org"
        );

        assertCollectionSize("orc-test", 15);
        XmlNode doc = readXmlDocument("/orc/Appolonia");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("o", "orc.org")});
        doc.assertElementValue("/o:myOrc/o:LastName", "Edeler");
    }
}
