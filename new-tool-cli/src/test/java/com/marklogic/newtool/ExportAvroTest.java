package com.marklogic.newtool;

import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExportAvroTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) {
        final String path = tempDir.toAbsolutePath().toString();

        run(
            "export_avro",
            "--query", "op.fromView('Medical', 'Authors', '')" +
                ".select(['CitationID', 'ForeName', 'LastName'])",
            "--path", path
        );

        List<Row> rows = preview(
            "import_avro",
            "--path", path
        );
        assertEquals(15, rows.size());
        Row row = rows.stream().filter(r -> r.getString(1).equals("Taite")).collect(Collectors.toList()).get(0);
        assertEquals(3l, row.getLong(0));
        assertEquals("Taite", row.getString(1));
        assertEquals("Shoebotham", row.getString(2));
    }
}
