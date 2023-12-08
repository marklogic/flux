package com.marklogic.newtool;

import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportXmlFilesTest extends AbstractTest {

    @Test
    void noNamespace() {
        List<Row> rows = run(
            "import_files",
            "--format", "xml",
            "--path", "src/test/resources/xml-files/books-no-namespace.xml",
            "-R:rowTag=book",
            "-R:ignoreSurroundingSpaces=true",
            "--preview"
        );
        assertEquals(12, rows.size());
    }

    @Test
    void withNamespace() {
        List<Row> rows = run(
            "import_files",
            "--format", "xml",
            "--path", "src/test/resources/xml-files/citations-with-namespace.xml",
            "-R:rowTag=ex:Citation",
            "--preview"
        );
        assertEquals(1, rows.size());
    }

    @Test
    void s3() {
        run(
            "import_files",
            "--format", "xml",
            "--path", "s3a://rudin-public-bucket/*.xml",
            "-R:rowTag=Employee",
            "--preview"
        );
    }
}
