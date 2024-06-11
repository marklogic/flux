package com.marklogic.flux.impl.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This re-implements some other tests that use our dedicated support for Parquet/Avro/CSV, as we just need to refer to
 * any Spark connector / data source.
 */
class CustomImportTest extends AbstractTest {

    @Test
    void parquet() {
        run(
            "custom_import",
            "--source", "parquet",
            "-Ppath=src/test/resources/parquet/individual/cars.parquet",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--xmlRootName", "my-parquet",
            "--xmlNamespace", "org:example",
            "--uriTemplate", "/parquet/{model}.xml"
        );
        assertCollectionSize("parquet-test", 32);

        XmlNode doc = readXmlDocument("/parquet/Merc 230.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example")});
        doc.assertElementValue("/ex:my-parquet/ex:mpg", "22.8");
    }

    @Test
    void avro() {
        run(
            "custom_import",
            "--source", "avro",
            "-Ppath=src/test/resources/avro",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "--uriTemplate", "/avro/{/avroData/color}.json",
            "--jsonRootName", "avroData"
        );

        assertCollectionSize("avro-test", 6);
        JsonNode doc = readJsonDocument("/avro/blue.json");
        assertEquals(1, doc.get("avroData").get("number").asInt());
    }

    @Test
    void csvWithDynamicParam() {
        run(
            "custom_import",
            "--source", "csv",
            "-Ppath=src/test/resources/delimited-files/semicolon-delimiter.csv",
            "-Pdelimiter=;",
            "-Pheader=true",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "csv-test",
            "--uriTemplate", "/csv/{number}.json"
        );

        assertCollectionSize("csv-test", 3);
        JsonNode doc = readJsonDocument("/csv/3.json");
        assertEquals("green", doc.get("color").asText());
    }

    /**
     * Depends on the spark-xml 3rd party connector (which is expected to be included in Spark 4 once that's released).
     */
    @Test
    void sparkXml() {
        run(
            "custom_import",
            // "xml" is associated with the external Spark XML connector from Databricks.
            "--source", "xml",
            "-Ppath=src/test/resources/xml-file/people.xml",
            "-ProwTag=person",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "spark-data",
            "--uriTemplate", "/company/{company}.json"
        );

        assertCollectionSize("spark-data", 3);
        JsonNode doc = readJsonDocument("/company/company-1.json");
        assertEquals("Person-1", doc.get("name").asText());
    }
}
