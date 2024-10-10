/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportFilesTest extends AbstractTest {

    private static final String[] MIXED_FILES_URIS = new String[]{"/hello.json", "/hello.txt", "/hello.xml", "/hello2.txt.gz"};

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
            "--uri-replace", ".*/mixed-files,''",

            // Including these for manual verification of progress logging.
            "--batch-size", "1",
            "--log-progress", "2",

            // Including for smoke testing and manual verification of logging.
            "--streaming"
        );

        verifyDocsWereWritten(MIXED_FILES_URIS.length, MIXED_FILES_URIS);
    }

    @Test
    void pathAndFilterWithWildcardInOptionsFile() {
        run(
            "import-files",
            "@src/test/resources/options-files/import-people-files.txt",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "people-files",
            "--uri-replace", ".*/xml-file,''"
        );

        assertCollectionSize(
            "Verifying that when --filter is included in an options file, double quotes should not be used for " +
                "the value of --filter.",
            "people-files", 2
        );
    }

    @Test
    void pathWithArityofTwo() {
        run(
            "import-files",
            "--path", "src/test/resources/mixed-files/hello.txt", "src/test/resources/mixed-files/hello.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "people-files",
            "--uri-replace", ".*/xml-file,''"
        );

        assertCollectionSize(
            "Verifying that --path accepts multiple values, which is needed in order for an asterisk to work " +
                "in some shell environments.",
            "people-files", 2
        );
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

        verifyDocsWereWritten(MIXED_FILES_URIS.length, MIXED_FILES_URIS);
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

    @Test
    void fileOptions(@TempDir Path tempDir) throws IOException {
        File optionsFile = new File(tempDir.toFile(), "options.txt");
        String options = "--path\n" +
            "src/test/resources/mixed-files/hello*\n" +
            "--connection-string\n" +
            makeConnectionString() + "\n" +
            "--uri-replace\n" +
            "\".*/mixed-files,''\"";
        FileCopyUtils.copy(options.getBytes(), optionsFile);

        run(
            "import-files",
            "@" + optionsFile.getAbsolutePath(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files"
        );

        verifyDocsWereWritten(MIXED_FILES_URIS.length, MIXED_FILES_URIS);
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

    @Test
    void customEncoding() {
        run(
            "import-files",
            "--path", "src/test/resources/encoding/medline.iso-8859-1.txt",
            "--connection-string", makeConnectionString(),
            "--encoding", "ISO-8859-1",
            "--collections", "encoded-xml",
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-replace", ".*/encoding,''",
            "--uri-suffix", ".xml"
        );

        assertCollectionSize("encoded-xml", 1);
        XmlNode doc = readXmlDocument("/medline.iso-8859-1.txt.xml");
        doc.assertElementExists("/MedlineCitationSet");
    }

    /**
     * Verifies that a file with Japanese characters in it - all valid UTF-8 - is imported correctly.
     */
    @Test
    void japanese() {
        run(
            "import-files",
            "--path", "src/test/resources/japanese-files/太田佳伸のＸＭＬファイル.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*resources,''"
        );

        assertCollectionSize("files", 1);
        XmlNode doc = readXmlDocument("/japanese-files/太田佳伸のＸＭＬファイル.xml");
        doc.assertElementValue("/root/filename", "太田佳伸のＸＭＬファイル");
    }

    @Test
    void splitNamespacedXml() {
        run(
            "import-files",
            "--path", "src/test/resources/xml-file/namespaced-java-client-intro.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "files",
            "--uri-replace", ".*/xml-file,''",
            "--splitter-xml-path", "/ex:root/ex:text/text()",
            "--splitter-xml-namespace", "ex=org:example",
            "--splitter-max-chunk-size", "500",
            "--splitter-max-overlap-size", "100",
            "--stacktrace"
        );

        XmlNode doc = readXmlDocument("/namespaced-java-client-intro.xml");
        doc.setNamespaces(new Namespace[]{Namespace.getNamespace("ex", "org:example")});
        doc.assertElementCount("The underlying langchain4j splitter is expected to produce 5 chunks when using a " +
            "max chunk size of 500 and a max overlap size of 100.", "/ex:root/chunks/chunk", 5);
    }

    private void verifyDocsWereWritten(int expectedUriCount, String... values) {
        List<String> uris = getUrisInCollection("files", values.length);
        assertEquals(expectedUriCount, uris.size());
        Stream.of(values).forEach(uri -> assertTrue(uris.contains(uri), String.format("Did not find %s in %s", uri, uris)));
    }
}
