/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
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
        Flux.customExportRows()
            .connectionString(makeConnectionString())
            .from(options -> options
                .opticQuery(READ_AUTHORS_OPTIC_QUERY)
                .partitions(1))
            .to(options -> options
                .target("xml")
                .additionalOptions(Map.of(
                    "path", tempDir.toFile().getAbsolutePath(),
                    "rootTag", "authors",
                    "rowTag", "author"
                ))
                .saveMode(SaveMode.OVERWRITE))
            .execute();

        // The name of this file may be an implementation detail of the connector and thus subject to change.
        File xmlFile = new File(tempDir.toFile(), "part-00000");
        XmlNode doc = new XmlNode(FileCopyUtils.copyToString(new FileReader(xmlFile)));
        doc.assertElementCount("/authors/author", 15);
        doc.assertElementExists("/authors/author[CitationID = 1 and LastName = 'Canham']");
    }
}
