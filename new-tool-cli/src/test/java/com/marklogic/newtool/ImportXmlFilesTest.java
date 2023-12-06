package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

public class ImportXmlFilesTest extends AbstractTest {

    @Test
    void noNamespace() {
        run(
            "import_files",
            "--format", "xml",
            "--path", "src/test/resources/xml-files/books-no-namespace.xml",
            "-R:rowTag=book",
            "-R:ignoreSurroundingSpaces=true",
            "--debug"
        );
    }

    @Test
    void withNamespace() {
        run(
            "import_files",
            "--format", "xml",
            "--path", "src/test/resources/xml-files/citations-with-namespace.xml",
            "-R:rowTag=ex:Citation",
            "--debug"
        );
    }
}
