package com.marklogic.newtool;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ExportFilesTest extends AbstractTest {

    /**
     * Demonstrates that Spark's "json" file support generates line-delimited JSON files by default.
     */
    @Test
    void json() throws Exception {
        // TODO Use a TempFile
        final String path = "build/export-json";
        FileUtils.deleteDirectory(new File(path));

        run(
            "export_files",
            "--format", "json",
            "--query", "op.fromView('Medical', 'Authors', '')" +
                ".where(op.sqlCondition('CitationID = 1'))" +
                ".select(['ForeName', 'LastName'])",
            "--path", path
        );

        run(
            "import_files",
            "--format", "json",
            "--path", path,
            "--uri-template", "/author/{ForeName}-{LastName}.json",
            "--collections", "json-author"
        );

        assertCollectionSize("json-author", 4);
        readJsonDocument("/author/Finlay-Awton.json", "json-author");
    }

    @Test
    void parquet() {
        run(
            "export_files",
            "--query", "op.fromView('Medical', 'Authors', '').where(op.sqlCondition('CitationID = 1'))",
            "--path", "build/testparquet"
        );

//        run(
//            "import_files",
//            "--path", "build/testparquet"
//        );
    }
}
