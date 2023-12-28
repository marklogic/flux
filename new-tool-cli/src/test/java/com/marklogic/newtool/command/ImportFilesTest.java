package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportFilesTest extends AbstractTest {

    @Test
    void test() {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--clientUri", makeClientUri(),
            "--collections", "files",
            "--uriReplace", ".*/mixed-files,''"
        );

        verifyFourDocsWereWritten();
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
            "src/test/resources/mixed-files\n" +
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

        verifyFourDocsWereWritten();
    }

    private void verifyFourDocsWereWritten() {
        List<String> uris = getUrisInCollection("files", 4);
        Stream.of("/hello.json", "/hello.txt", "/hello.xml", "/hello2.txt.gz").forEach(uri -> {
            assertTrue(uris.contains(uri), String.format("Did not find %s in %s", uri, uris));
        });
    }
}
