/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Disabled;
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

    String[] uris = new String[]{"/hello.json", "/hello.txt", "/hello.xml", "/hello2.txt.gz"};

    @Test
    void multiplePaths() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*txt*",
            "--path", "src/test/resources/mixed-files/hello.json",
            "--path", "src/test/resources/mixed-files/hello.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''"
        );

        verifyDocsWereWritten(uris.length, uris);
    }

    @Test
    void withUsernameAndPasswordAndAuthType() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--host", getDatabaseClient().getHost(),
            "--port", getDatabaseClient().getPort() + "",
            "--username", DEFAULT_USER,
            "--password", DEFAULT_PASSWORD,
            "--auth-type", "digest",
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''"
        );

        verifyDocsWereWritten(uris.length, uris);
    }

    @Test
    void invalidAuthType() {
        assertStderrContains(() -> run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--auth-type", "notvalid"
        ), "Invalid value for option '--auth-type': expected one of [BASIC, DIGEST, CLOUD, KERBEROS, CERTIFICATE, SAML] (case-insensitive) but was 'notvalid'");
    }

    @Test
    void documentType() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''",
            "--uri-suffix", ".unknown",
            "--document-type", "xml"
        );

        String kind = getDatabaseClient().newServerEval()
            .xquery("xdmp:node-kind(doc('/hello.xml.unknown')/node())")
            .evalAs(String.class);
        assertEquals("element", kind);
    }

    /**
     * preview = show the first N rows from the reader, and don't invoke the writer.
     */
    @Test
    @Disabled("Another stdout test that runs fine by itself, but fails when the suite is run.")
    void preview() {
        String stdout = runAndReturnStdout(() -> run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--preview", "2",
            "--preview-drop", "content", "modificationTime",
            "--preview-vertical"
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
            "--connection-string\n" +
            makeConnectionString() + "\n" +
            "--uri-replace\n" +
            ".*/mixed-files,''";
        FileCopyUtils.copy(options.getBytes(), optionsFile);

        run(
            "import-files",
            "@" + optionsFile.getAbsolutePath(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        verifyDocsWereWritten(uris.length, uris);
    }

    @Test
    void zipTest() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''",
            "--compression", "zip"
        );

        verifyDocsWereWritten(3, "/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml");
    }

    @Test
    void zipCaseSensitivityTest() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/goodbye.zip",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''",
            "--compression", "ZIp"
        );

        verifyDocsWereWritten(3, "/goodbye.zip/goodbye.json", "/goodbye.zip/goodbye.txt", "/goodbye.zip/goodbye.xml");
    }

    @Test
    void gzipTest() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello2.txt.gz",
            "--connection-string", makeConnectionString(),
            "--collections", "files",
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/mixed-files,''",
            "--compression", "gzip"
        );

        verifyDocsWereWritten(1, "/hello2.txt");
    }

    @Test
    void fileOptionsFilter() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/mixed-files,''",
            "--filter", "*.json"
        );

        verifyDocsWereWritten(1, "/hello.json");
    }

    @Test
    void fileOptionsRecursiveFileLookupDefault() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--filter", "*.json",
            "--uri-replace", ".*/mixed-files,''"
        );

        assertCollectionSize("files", 2);
    }

    @Test
    void fileOptionsRecursiveFileLookupFalse() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--filter", "*.json",
            "--recursive-file-lookup", "false",
            "--uri-replace", ".*/mixed-files,''"
        );

        assertCollectionSize("files", 1);
    }

    @Test
    void invalidGzippedFile() {
        run(
            "import-files",
            "--path", "src/test/resources/json-files/array-of-objects.json",
            "--path", "src/test/resources/mixed-files/hello2.txt.gz",
            "--connection-string", makeConnectionString(),
            "--collections", "files",
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/mixed-files,''",
            "--compression", "gzip"
        );

        assertCollectionSize("The command should default to not aborting on failure, and thus an error should be " +
            "logged for the JSON file and the gz file should still be processed.", "files", 1);
        verifyDocsWereWritten(1, "/hello2.txt");
    }

    @Test
    void abortOnReadFailure() {
        String stderr = runAndReturnStderr(() -> run(
            "import-files",
            "--path", "src/test/resources/json-files/array-of-objects.json",
            "--abort-on-read-failure",
            "--connection-string", makeConnectionString(),
            "--collections", "files",
            "--permissions", DEFAULT_PERMISSIONS,
            "--compression", "gzip"
        ));

        assertTrue(stderr.contains("Command failed, cause: Unable to read file at"), "With --abort-read-on-failure, " +
            "the command should fail when it encounters an invalid gzipped file.");
        assertCollectionSize("files", 0);
    }

    private void verifyDocsWereWritten(int expectedUriCount, String... values) {
        List<String> uris = getUrisInCollection("files", values.length);
        assertEquals(expectedUriCount, uris.size());
        Stream.of(values).forEach(uri -> assertTrue(uris.contains(uri), String.format("Did not find %s in %s", uri, uris)));
    }
}
