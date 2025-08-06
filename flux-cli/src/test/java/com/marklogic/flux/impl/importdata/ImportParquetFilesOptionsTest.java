/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ImportParquetFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void configurationAndDataSourceOptions() {
        ImportParquetFilesCommand command = (ImportParquetFilesCommand) getCommand(
            "import-parquet-files",
            "--path", "/doesnt/matter",
            "-PdatetimeRebaseMode=CORRECTED",
            "-Cspark.sql.parquet.filterPushdown=false",
            "--preview", "10"
        );

        Map<String, String> options = command.getReadParams().makeOptions();
        assertOptions(options, "datetimeRebaseMode", "CORRECTED");
        assertFalse(options.containsKey("spark.sql.parquet.filterPushdown"),
            "Dynamic params starting with 'spark.sql' should not be added to the 'read' options. They should " +
                "instead be added to the SparkConf object, per the documentation at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html .");
    }
}
