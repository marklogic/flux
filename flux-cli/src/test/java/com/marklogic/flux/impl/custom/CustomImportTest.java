/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
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
            "custom-import",
            "--source", "parquet",
            "--spark-prop", "path=src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "parquet-test",
            "--xml-root-name", "my-parquet",
            "--xml-namespace", "org:example",
            "--uri-template", "/parquet/{model}.xml"
        );
        assertCollectionSize("parquet-test", 32);

        XmlNode doc = readXmlDocument("/parquet/Merc 230.xml");
        doc.assertElementValue("/ex:my-parquet/ex:mpg", "22.8");
    }

    @Test
    void avro() {
        run(
            "custom-import",
            "--source", "avro",
            "--spark-prop", "path=src/test/resources/avro",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "avro-test",
            "--uri-template", "/avro/{/avroData/color}.json",
            "--json-root-name", "avroData"
        );

        assertCollectionSize("avro-test", 6);
        JsonNode doc = readJsonDocument("/avro/blue.json");
        assertEquals(1, doc.get("avroData").get("number").asInt());
    }

    @Test
    void csvWithDynamicParam() {
        run(
            "custom-import",
            "--source", "csv",
            "--spark-prop", "path=src/test/resources/delimited-files/semicolon-delimiter.csv",
            "--spark-prop", "delimiter=;",
            "--spark-prop", "header=true",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "csv-test",
            "--uri-template", "/csv/{number}.json"
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
            "custom-import",
            // "xml" is associated with the external Spark XML connector from Databricks.
            "--source", "xml",
            "--spark-prop", "path=src/test/resources/xml-file/people.xml",
            "--spark-prop", "rowTag=person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "spark-data",
            "--uri-template", "/company/{company}.json"
        );

        assertCollectionSize("spark-data", 3);
        JsonNode doc = readJsonDocument("/company/company-1.json");
        assertEquals("Person-1", doc.get("name").asText());
    }
}
