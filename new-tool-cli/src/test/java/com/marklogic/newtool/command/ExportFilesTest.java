package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.newtool.AbstractTest;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportFilesTest extends AbstractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void exportToRegularFiles(@TempDir Path tempDir) throws Exception {
        run(
            "export_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--clientUri", makeClientUri(),
            "--collections", "author"
        );

        File dir = tempDir.toFile();
        assertEquals(1, dir.listFiles().length, "Expecting a single 'author' directory in the temp dir");
        assertEquals("author", dir.listFiles()[0].getName());

        File authorsDir = new File(dir, "author");
        assertEquals(15, authorsDir.listFiles().length, "Expecting 15 files, 1 for each of the 15 author documents");

        // Do a small verification of each file to ensure it's proper JSON.
        for (int i = 1; i <= 15; i++) {
            File file = new File(authorsDir, "author" + i + ".json");
            JsonNode doc = objectMapper.readTree(file);
            assertTrue(doc.has("CitationID"));
            assertTrue(doc.has("LastName"));
        }
    }

    @Test
    void exportToZips(@TempDir Path tempDir) {
        run(
            "export_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "zip",
            "--clientUri", makeClientUri(),
            "--collections", "author"
        );

        File dir = tempDir.toFile();
        assertEquals(3, dir.listFiles().length, "Expecting 3 zip files - 1 for each forest in the test database, " +
            "which is expected to have 3 forests.");

        // Use our connector to read the 3 zip files, can then verify the dataset.
        List<Row> rows = newSparkSession().read().format("marklogic")
            .option(Options.READ_FILES_COMPRESSION, "zip")
            .load(dir.getAbsolutePath())
            .collectAsList();

        assertEquals(15, rows.size());

        List<String> filenames = rows.stream().map(row -> {
            String path = row.getString(0);
            return path.substring(path.lastIndexOf("/") + 1);
        }).collect(Collectors.toList());

        for (int i = 1; i <= 15; i++) {
            String expectedFilename = "author" + i + ".json";
            assertTrue(filenames.contains(expectedFilename),
                "Did not find " + expectedFilename + " in " + filenames);
        }
    }

    @Test
    void exportToZipsWithRepartition(@TempDir Path tempDir) {
        run(
            "export_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "zip",
            "--clientUri", makeClientUri(),
            "--collections", "author",
            "--repartition", "5"
        );

        File dir = tempDir.toFile();
        assertEquals(5, dir.listFiles().length, "Should have 5 zip files instead of 3 due to the use of --repartition.");
    }

    @Test
    void exportToGZippedFiles(@TempDir Path tempDir) {
        run(
            "export_files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "gzip",
            "--clientUri", makeClientUri(),
            "--collections", "author"
        );

        File dir = tempDir.toFile();
        assertEquals(1, dir.listFiles().length, "Expecting a single 'author' directory in the temp dir");
        assertEquals("author", dir.listFiles()[0].getName());

        File authorsDir = new File(dir, "author");
        assertEquals(15, authorsDir.listFiles().length, "Expecting 15 gzipped files, 1 for each of the 15 author documents");
        for (File file : authorsDir.listFiles()) {
            assertTrue(file.getName().endsWith(".gz"));
        }
    }
}
