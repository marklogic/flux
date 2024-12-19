/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregateXmlFilesImporterTest extends AbstractTest {

    @Test
    void uriElementAndNamespace() {
        Flux.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/xml-file/people-with-namespace.xml")
                .element("person")
                .namespace("org:example")
                .uriElement("name")
                .uriNamespace("org:example"))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("with-all-options")
                .uriSuffix(".xml"))
            .execute();

        assertCollectionSize("with-all-options", 3);
        Stream.of("Person-1.xml", "Person-2.xml", "Person-3.xml").forEach(uri -> {
            XmlNode doc = readXmlDocument(uri);
            doc.assertElementExists("/ex:person/ex:name");
        });
    }

    @Test
    void zip() {
        Flux.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/xml-file/single-xml.zip")
                .compressionType(CompressionType.ZIP)
                .element("person"))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("zipped-person"))
            .execute();

        assertCollectionSize("zipped-person", 3);
    }

    @Test
    void missingElement() {
        AggregateXmlFilesImporter importer = Flux.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/xml-file/single-xml.zip"))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("zipped-person"));

        FluxException ex = assertThrowsFluxException(() -> importer.execute());
        assertEquals("Must specify an aggregate XML element name", ex.getMessage());
    }

    @Test
    void missingPath() {
        AggregateXmlFilesImporter importer = Flux.importAggregateXmlFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .compressionType(CompressionType.ZIP)
                .element("person"))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("zipped-person"));

        FluxException ex = assertThrowsFluxException(() -> importer.execute());
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }
}
