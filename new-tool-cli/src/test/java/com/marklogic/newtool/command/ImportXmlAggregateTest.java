package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

public class ImportXmlAggregateTest extends AbstractTest {

    @Test
    void defaultSettings() {

        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "xml-test",
            "--uriTemplate", "/xml/test.txt"
        );
    }

    @Test
    void withElement() {

        run(
            "import_aggregate_xml_files",
            "--path", "src/test/resources/xml-file/people.xml",
            "--element", "person",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "xml-test",
            "--uriTemplate", "/xml/test.txt"
        );
    }
}
