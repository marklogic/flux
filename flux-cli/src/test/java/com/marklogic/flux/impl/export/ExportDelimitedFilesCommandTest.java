/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportDelimitedFilesCommandTest extends AbstractTest {

    @Test
    void test(@TempDir Path tempDir) throws IOException {
        run(
            "export-delimited-files",
            "--connection-string", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--file-count", "1"
        );

        verifyDelimitedFile(tempDir);
    }

    @Test
    void exportTwice(@TempDir Path tempDir) {
        run(
            "export-delimited-files",
            "--connection-string", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '')",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--file-count", "1"
        );

        run(
            "export-delimited-files",
            "--connection-string", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '')",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--file-count", "1"
        );

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(2, files.length, "Per MLE-16859, commands that use Spark file data sources for exporting data " +
            "should default to 'append' instead of 'overwrite' to avoid accidentally deleting data.");
    }

    /**
     * Verifies that an options file can have values with whitespace in them, which picocli supports and
     * JCommander oddly does not. Users will frequently have spaces in any Optic or search queries that they put into
     * options files. And it's common to put queries into options files as they are awkward to type directly on the
     * command line.
     *
     * @param tempDir
     * @throws IOException
     */
    @Test
    void optionsFileWithWhitespaceInValues(@TempDir Path tempDir) throws IOException {
        run(
            "export-delimited-files",
            "--connection-string", makeConnectionString(),
            "@src/test/resources/options-files/export-rows.txt",
            "--path", tempDir.toFile().getAbsolutePath()
        );

        verifyDelimitedFile(tempDir);
    }

    private void verifyDelimitedFile(Path tempDir) throws IOException {
        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);

        String content = FileCopyUtils.copyToString(new FileReader(files[0]));
        String[] lines = content.split("\n");
        assertEquals(16, lines.length, "The header=true option should be included by default.");
        assertEquals("CitationID,LastName,ForeName,Date,DateTime,LuckyNumber,Base64Value,BooleanValue,CalendarInterval", lines[0]);
        assertEquals("1,Awton,Finlay,2022-07-13,2022-07-13T09:00:00.000Z,4,,,", lines[1]);
    }

    @Test
    void headerRemovedViaDynamicParam(@TempDir Path tempDir) throws IOException {
        run(
            "export-delimited-files",
            "--connection-string", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-Pheader=false",
            "--file-count", "1"
        );

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);

        String content = FileCopyUtils.copyToString(new FileReader(files[0]));
        String[] lines = content.split("\n");
        assertEquals(15, lines.length);
        assertEquals("1,Awton,Finlay,2022-07-13,2022-07-13T09:00:00.000Z,4,,,", lines[0]);
    }
}
