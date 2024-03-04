package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.newtool.command.ImportAvroFilesCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ImportAvroFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportAvroFilesCommand command = (ImportAvroFilesCommand) getCommand(
            "import_avro_files",
            "--path", "/doesnt/matter",
            "-PdatetimeRebaseMode=CORRECTED",
            "-Pspark.sql.parquet.filterPushdown=false",
            "--preview", "10"
        );

        Map<String, String> options = command.makeReadOptions();
        assertOptions(options, "datetimeRebaseMode", "CORRECTED");
        assertFalse(options.containsKey("spark.sql.parquet.filterPushdown"),
            "Dynamic params starting with 'spark.sql' should not be added to the 'read' options. They should " +
                "instead be added to the SparkConf object, per the documentation at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-avro.html.");
    }
}
