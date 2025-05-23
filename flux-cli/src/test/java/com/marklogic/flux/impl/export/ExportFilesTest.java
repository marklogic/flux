/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExportFilesTest extends AbstractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void exportToRegularFiles(@TempDir Path tempDir) throws Exception {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            // Including this simply to verify that it doesn't cause an error. Its impact is only going to be seen
            // in performance tests.
            "--batch-size", "5"
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

    @ParameterizedTest
    @ValueSource(strings = {"doesntexist", "has space", "has+plus"})
    void pathDoesntExist(String directoryName, @TempDir Path tempDir) {
        File dir = new File(tempDir.toFile(), directoryName);
        assertFalse(dir.exists());

        run(
            "export-files",
            "--path", dir.getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--uris", "/citations.xml"
        );

        assertTrue(dir.exists(), "Directory was not created: " + dir.getAbsolutePath());
        assertEquals(1, dir.listFiles().length);
        assertTrue(new File(dir, "citations.xml").exists());
    }

    @Test
    void exportViaQueryInOptionsFile(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "@src/test/resources/options-files/cts-query-json.txt"
        );

        File dir = tempDir.toFile();
        File authorsDir = new File(dir, "author");
        assertEquals(1, authorsDir.listFiles().length, "Expecting the query to only retrieve the 'Vivienne' author.");
    }

    @Test
    void exportViaUris(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--uris", "/author/author1.json\n/author/author2.json"
        );

        File authorsDir = new File(tempDir.toFile(), "author");
        assertEquals(2, authorsDir.listFiles().length);
        assertTrue(new File(authorsDir, "author1.json").exists());
        assertTrue(new File(authorsDir, "author2.json").exists());
    }

    @Test
    void exportToZips(@TempDir Path tempDir) {
        run(
            "export-files",
            "--partitions-per-forest", "1",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "zip",
            "--connection-string", makeConnectionString(),
            "--collections", "author"
        );

        File dir = tempDir.toFile();
        assertEquals(3, dir.listFiles().length, "Expecting 3 ZIP files - 1 for each forest in the test database, " +
            "which is expected to have 3 forests.");

        // Use our connector to read the 3 ZIP files, can then verify the dataset.
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
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "zip",
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--zip-file-count", "5"
        );

        File dir = tempDir.toFile();
        assertEquals(5, dir.listFiles().length, "Should have 5 ZIP files instead of 3 due to the use of --zip-file-count.");
    }

    @Test
    void exportToGZippedFiles(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "gzip",
            "--connection-string", makeConnectionString(),
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

    @Test
    void exportWithNoQuery(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--compression", "zip",
            "--zip-file-count", "1",
            "--connection-string", makeConnectionString()
        );

        long countOfExportedDocuments = newSparkSession()
            .read().format("marklogic")
            .option(Options.READ_FILES_COMPRESSION, "zip")
            .load(tempDir.toFile().getAbsolutePath())
            .count();

        QueryManager queryManager = getDatabaseClient().newQueryManager();
        StructuredQueryDefinition allDocumentsQuery = queryManager.newStructuredQueryBuilder().and();
        SearchHandle searchHandle = queryManager.search(allDocumentsQuery, new SearchHandle());

        assertEquals(searchHandle.getTotalResults(), countOfExportedDocuments, "As of the Flux 1.2.0 release, if no " +
            "query option is specified, then Flux should use a true-query as the query so that all documents are " +
            "selected.");
    }

    @Test
    void withRedactionTransform(@TempDir Path tempDir) throws Exception {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--uris", "/jane.json",
            "--connection-string", makeConnectionString(),
            "--transform", "redacter",
            "--transform-params", "rulesetName,email-rules"
        );

        assertEquals(1, tempDir.toFile().listFiles().length);
        JsonNode doc = objectMapper.readTree(tempDir.toFile().listFiles()[0]);
        assertEquals("Jane", doc.get("name").asText());
        assertEquals("NAME@DOMAIN", doc.get("email").asText(),
            "This verifies that the transform is able to use the MarkLogic redaction library, as the flux-test-role " +
                "role is granted the redaction-user privilege.");
    }

    /**
     * Verifies that an error containing "XDMP-OLDSTAMP" (which is very difficult to cause to happen in a test, so a
     * REST transform is used as a way to simulate such an error) results in additional information being printed so
     * that the user can resolve the error more easily.
     */
    @Test
    void withTransformThatThrowsTimestampError() {
        assertStderrContains(() -> run(
                "export-files",
                "--path", ".",
                "--uris", "/jane.json",
                "--connection-string", makeConnectionString(),
                "--transform", "throwsTimestampError"
            ),
            "To resolve an XDMP-OLDSTAMP error, consider using the --no-snapshot option"
        );
    }

    @Test
    void secondaryUrisJavascript(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--uris", "/author/author1.json\n/author/author2.json",
            "--secondary-uris-javascript", "var URIs;" +
                "const citationIds = cts.elementValues(xs.QName('CitationID'), null, null, cts.documentQuery(URIs));" +
                "cts.uris(null, null, cts.andQuery([ " +
                "   cts.notQuery(cts.documentQuery(URIs)), " +
                "   cts.collectionQuery('author'), " +
                "   cts.jsonPropertyValueQuery('CitationID', citationIds)" +
                "]))"
        );

        verifyFiveAuthorsAreWritten(tempDir);
    }

    @Test
    void secondaryUrisJavascriptFile(@TempDir Path tempDir) {
        run(
            "export-files",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--connection-string", makeConnectionString(),
            "--uris", "/author/author1.json\n/author/author2.json",
            "--secondary-uris-javascript-file", "src/test/resources/custom-code/secondary-uris-citation-id.js"
        );

        verifyFiveAuthorsAreWritten(tempDir);
    }

    private void verifyFiveAuthorsAreWritten(Path tempDir) {
        File authorsDir = new File(tempDir.toFile(), "author");
        assertEquals(5, authorsDir.listFiles().length, "Expecting 5 files - the 2 original URIs and 3 secondary URIs. " +
            "The secondary URIs come from the query on the CitationID property.");

        Stream.of("author1.json", "author2.json",
                "author4.json", "author8.json", "author14.json")
            .forEach(uri -> assertTrue(new File(authorsDir, uri).exists(), "Expecting to find URI: " + uri));
    }
}
