/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.Options;
import org.apache.spark.sql.SaveMode;
import org.junit.jupiter.api.Test;

class ImportTsvFilesTest extends AbstractTest {

    @Test
    void thisWorks() {
        newSparkSession()
            .read()
            // Flux sets both of these by default.
            .option("header", "true")
            .option("inferSchema", true)

            // Flux sets this via --delimiter .
            .option("sep", "\t")

            // This maps to "--path".
            .csv("src/test/resources/tsv-files")

            .write().format("marklogic")
            .option(Options.CLIENT_URI, makeConnectionString())
            .option(Options.WRITE_PERMISSIONS, DEFAULT_PERMISSIONS)
            .option(Options.WRITE_COLLECTIONS, "tsv-test")
            .mode(SaveMode.Append)
            .save();

        assertCollectionSize("tsv-test", 10);
    }

    @Test
    void thisDoesntWork() {
        run(
            "import-delimited-files",
            "--path", "src/test/resources/tsv-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "tsv-test",
            "--delimiter", "\t"
        );

        // Somehow we're only getting the 5 rows from one file, not both files.
        assertCollectionSize("tsv-test", 10);
    }
}
