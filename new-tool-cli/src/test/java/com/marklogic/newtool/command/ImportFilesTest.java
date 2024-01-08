package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.apache.spark.SparkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ImportFilesTest extends AbstractTest {

    String[] uris = new String[] {"/hello.json", "/hello.txt", "/hello.xml", "/hello2.txt.gz"};

    @Test
    void test() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--clientUri", makeClientUri(),
            "--collections", "files",
            "--uriReplace", ".*/mixed-files,''"
        );

        verifyDocsWereWritten(uris);
    }

    /**
     * preview = show the first N rows from the reader, and don't invoke the writer.
     */
    @Test
    void preview() {
        String stdout = runAndReturnStdout(() -> run(
            "import_files",
            "--path", "src/test/resources/mixed-files",
            "--preview", "2",
            "--previewDrop", "content", "modificationTime"
        ));

        String message = "Unexpected output to stdout: " + stdout;
        assertTrue(stdout.contains("RECORD 0"), message);
        assertTrue(stdout.contains("RECORD 1"), message);
        assertFalse(stdout.contains("RECORD 2"), message);
        assertTrue(stdout.contains("path"), message);
        assertTrue(stdout.contains("length"), message);
        assertFalse(stdout.contains("content"), message);
        assertFalse(stdout.contains("modificationTime"), message);
    }

    /**
     * Verifies that https://jcommander.org/#_syntax "just works"!
     */
    @Test
    void fileOptions(@TempDir Path tempDir) throws IOException {
        File optionsFile = new File(tempDir.toFile(), "options.txt");
        String options = "--path\n" +
            "src/test/resources/mixed-files/hello*\n" +
            "--clientUri\n" +
            makeClientUri() + "\n" +
            "--uriReplace\n" +
            ".*/mixed-files,''";
        FileCopyUtils.copy(options.getBytes(), optionsFile);

        run(
            "import_files",
            "@" + optionsFile.getAbsolutePath(),
            "--collections", "files"
        );

        verifyDocsWereWritten(uris);
    }

    @Test
    void zipTest() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--clientUri", makeClientUri(),
            "--collections", "files",
            "--uriReplace", ".*/mixed-files,''",
            "--compression", "zip"
        );

        verifyDocsWereWritten("/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml");
    }

    @Test
    void zipCaseSensitivityTest() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--clientUri", makeClientUri(),
            "--collections", "files",
            "--uriReplace", ".*/mixed-files,''",
            "--compression", "ZIp"
        );

        verifyDocsWereWritten("/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml");
    }

    @Test
    void gzipTest() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello2.txt.gz",
            "--clientUri", makeClientUri(),
            "--collections", "files",
            "--uriReplace", ".*/mixed-files,''",
            "--compression", "gzip"
        );

        verifyDocsWereWritten("/hello2.txt");
    }

    @Test
    void badGzipTest() {
        try {
            run(
                "import_files",
                "--path", "src/test/resources/mixed-files/goodbye.zip",
                "--clientUri", makeClientUri(),
                "--collections", "files",
                "--uriReplace", ".*/mixed-files,''",
                "--compression", "gzip"
            );
        } catch (Exception e) {
            if (e instanceof SparkException) {
                assertTrue(e.getCause().getMessage().contains("Unable to read gzip file"));
                return;
            } else {
                fail("An unexpected exception was thrown.");
            }
        }
        fail("The test end in the catch block.");
    }

    private void verifyDocsWereWritten(String... values) {
        List<String> uris = getUrisInCollection("files", values.length);
        Stream.of(values).forEach(uri -> assertTrue(uris.contains(uri), String.format("Did not find %s in %s", uri, uris)));
    }
}
