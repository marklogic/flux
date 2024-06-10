package com.marklogic.newtool.impl.custom;

import com.marklogic.junit5.XmlNode;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

class CustomExportRowsTest extends AbstractTest {

    @Test
    void sparkXml(@TempDir Path tempDir) throws IOException {
        run(
            "custom-export-rows",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--target", "xml",
            "--repartition", "1",
            "-Ppath=" + tempDir.toFile().getAbsolutePath(),
            "-ProotTag=authors",
            "-ProwTag=author",
            "--mode", "Overwrite"
        );

        // The name of this file may be an implementation detail of the connector and thus subject to change.
        File xmlFile = new File(tempDir.toFile(), "part-00000");
        XmlNode doc = new XmlNode(FileCopyUtils.copyToString(new FileReader(xmlFile)));
        doc.assertElementCount("/authors/author", 15);
        doc.assertElementExists("/authors/author[CitationID = 1 and LastName = 'Canham']");
    }
}
