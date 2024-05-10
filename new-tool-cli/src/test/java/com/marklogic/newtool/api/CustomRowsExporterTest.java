package com.marklogic.newtool.api;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.apache.spark.sql.SaveMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

class CustomRowsExporterTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) throws IOException {
        NT.customExportRows()
            .connectionString(makeConnectionString())
            .readRows(options -> options
                .opticQuery(READ_AUTHORS_OPTIC_QUERY)
                .partitions(1))
            .writeRows(options -> options
                .target("xml")
                .additionalOptions(Map.of(
                    "path", tempDir.toFile().getAbsolutePath(),
                    "rootTag", "authors",
                    "rowTag", "author"
                ))
                .saveMode(SaveMode.Overwrite))
            .execute();

        // The name of this file may be an implementation detail of the connector and thus subject to change.
        File xmlFile = new File(tempDir.toFile(), "part-00000");
        XmlNode doc = new XmlNode(FileCopyUtils.copyToString(new FileReader(xmlFile)));
        doc.assertElementCount("/authors/author", 15);
        doc.assertElementExists("/authors/author[CitationID = 1 and LastName = 'Canham']");
    }
}
