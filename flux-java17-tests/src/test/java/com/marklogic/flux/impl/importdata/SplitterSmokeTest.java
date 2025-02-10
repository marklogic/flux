/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractJava17Test;
import com.marklogic.flux.impl.PostgresUtil;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitterSmokeTest extends AbstractJava17Test {

    @Test
    void aggregateJsonFiles() {
        run(
            "import-aggregate-json-files",
            "--path", "../flux-cli/src/test/resources/json-files/aggregates/array-of-objects.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "json-objects",
            "--uri-template", "/json-object/{number}.json",
            "--splitter-json-pointer", "/description",
            "--splitter-max-chunk-size", "30"
        );

        JsonNode doc = readJsonDocument("/json-object/2.json");
        assertEquals(2, doc.get("chunks").size());
    }

    @Test
    void aggregateXmlFiles() {
        run(
            "import-aggregate-xml-files",
            "--path", "../flux-cli/src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/xml-file,''",
            "--splitter-xpath", "/person/company/text()"
        );

        XmlNode doc = readXmlDocument("/people.xml-1.xml");
        doc.assertElementValue("/person/model:chunks/model:chunk/model:text", "company-1");
    }

    @Test
    void archiveFiles() {
        run(
            "import-archive-files",
            "--path", "../flux-cli/src/test/resources/archive-files",
            "--uri-replace", ".*archive.zip,''",
            "--connection-string", makeConnectionString(),
            "--splitter-xpath", "/hello/text()"
        );

        XmlNode doc = readXmlDocument("/test/1.xml");
        doc.assertElementValue("This may not be desirable - when there's a single root element " +
                "with a text node, the chunks gets added to the root element. It's valid to do this, but it may " +
                "also be a little surprising. Though, it's likely rare to have an XML document with a single root " +
                "element and text node.",
            "/hello/model:chunks/model:chunk/model:text", "world");
    }

    @Test
    void avroFiles() {
        run(
            "import-avro-files",
            "--path", "../flux-cli/src/test/resources/avro/*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--json-root-name", "myAvroData",
            "--uri-template", "/avro/{/myAvroData/color}.json",
            "--splitter-json-pointer", "/myAvroData/color"
        );

        JsonNode doc = readJsonDocument("/avro/blue.json");
        assertEquals("blue", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void copyDocuments() {
        run(
            "copy",
            "--categories", "content",
            "--collections", "author",
            "--connection-string", makeConnectionString(),
            "--output-collections", "author-copies",
            "--output-uri-prefix", "/copied",
            "--output-permissions", DEFAULT_PERMISSIONS,
            "--splitter-json-pointer", "/ForeName"
        );

        JsonNode doc = readJsonDocument("/copied/author/author4.json");
        assertEquals("Rani", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void delimitedFiles() {
        run(
            "import-delimited-files",
            "--path", "../flux-cli/src/test/resources/delimited-files/three-rows.csv",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/delimited/{/number}.json",
            "--splitter-json-pointer", "/color"
        );

        JsonNode doc = readJsonDocument("/delimited/1.json");
        assertEquals("blue", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void jdbc() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 2",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{/customer_id}.json",
            "--splitter-json-pointer", "/first_name"
        );

        JsonNode doc = readJsonDocument("/customer/1.json");
        assertEquals("Mary", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void mlcpArchiveFiles() {
        run(
            "import-mlcp-archive-files",
            "--path", "../flux-cli/src/test/resources/mlcp-archives",
            "--connection-string", makeConnectionString(),
            "--splitter-xpath", "/hello/text()"
        );

        XmlNode doc = readXmlDocument("/test/1.xml");
        doc.assertElementValue("/hello/model:chunks/model:chunk/model:text", "world");
    }

    @Test
    void orcFiles() {
        run(
            "import-orc-files",
            "--path", "../flux-cli/src/test/resources/orc-files/authors.orc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/orc-test/{LastName}.json",
            "--splitter-json-pointer", "/LastName"
        );

        JsonNode doc = readJsonDocument("/orc-test/Awton.json");
        assertEquals("Awton", doc.get("chunks").get(0).get("text").asText());
    }

    @Test
    void parquetFiles() {
        run(
            "import-parquet-files",
            "--path", "../flux-cli/src/test/resources/parquet/individual/cars.parquet",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/parquet/{model}.json",
            "--splitter-json-pointer", "/model"
        );

        JsonNode doc = readJsonDocument("/parquet/Valiant.json");
        assertEquals("Valiant", doc.get("chunks").get(0).get("text").asText());
    }


    /**
     * The splitter capability is not expected to be useful on managed triples documents. But allowing for splitter
     * options to be set greatly simplifies both the set of import commands and the import API interfaces - i.e. putting
     * SplitterParams in WriteDocumentParams is much easier to maintain. It's also a little simpler to say that
     * splitting is supported on all import commands instead of all import commands except RDF.
     */
    @Test
    void rdfFiles() {
        run(
            "import-rdf-files",
            "--path", "../flux-cli/src/test/resources/rdf/englishlocale.ttl",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "my-triples",
            "--splitter-xpath", "/text"
        );

        assertCollectionSize(
            "The default MarkLogic graph collection should contain a sem:graph document and a single managed " +
                "triples document containing the imported triples.", DEFAULT_MARKLOGIC_GRAPH, 2
        );
    }
}
