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
            "export_delimited_files",
            "--connectionString", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))",
            "--path", tempDir.toFile().getAbsolutePath(),
            "--fileCount", "1"
        );

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
            "export_delimited_files",
            "--connectionString", makeConnectionString(),
            "--partitions", "1",
            "--query", "op.fromView('Medical', 'Authors', '').orderBy(op.asc(op.col('LastName')))",
            "--path", tempDir.toFile().getAbsolutePath(),
            "-Pheader=false",
            "--fileCount", "1"
        );

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
        assertEquals(1, files.length);

        String content = FileCopyUtils.copyToString(new FileReader(files[0]));
        String[] lines = content.split("\n");
        assertEquals(15, lines.length);
        assertEquals("1,Awton,Finlay,2022-07-13,2022-07-13T09:00:00.000Z,4,,,", lines[0]);
    }
}
