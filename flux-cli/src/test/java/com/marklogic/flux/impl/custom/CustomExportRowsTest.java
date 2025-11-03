/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.junit5.XmlNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

class CustomExportRowsTest extends AbstractTest {

    @Disabled("Need a different data source since xml is part of Spark 4")
    @Test
    void sparkXml(@TempDir Path tempDir) throws IOException {
        run(
            "custom-export-rows",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY + ".select(['CitationID', 'LastName'])",
            "--target", "xml",
            "--repartition", "1",
            "--spark-prop", "path=" + tempDir.toFile().getAbsolutePath(),
            "--spark-prop", "rootTag=authors",
            "--spark-prop", "rowTag=author",
            "--mode", "Overwrite"
        );

        // The name of this file may be an implementation detail of the connector and thus subject to change.
        File xmlFile = new File(tempDir.toFile(), "part-00000");
        XmlNode doc = new XmlNode(FileCopyUtils.copyToString(new FileReader(xmlFile)));
        doc.assertElementCount("/authors/author", 15);
        doc.assertElementExists("/authors/author[CitationID = 1 and LastName = 'Canham']");
    }
}
