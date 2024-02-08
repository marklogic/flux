package com.marklogic.newtool.command;

import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class ImportOrcFilesTest extends AbstractTest {

    @Test
    void orcFileTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/orc-file.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test",
            "--uriPrefix", "/orc-test",
            "--uriReplace", ".json,''"
        );

        assertCollectionSize("orcFile-test", 5);
        verifyDocContent("/orc-test/author/author12.json");
        verifyDocContent("/orc-test/author/author2.json");
        verifyDocContent("/orc-test/author/author5.json");
        verifyDocContent("/orc-test/author/author6.json");
        verifyDocContent("/orc-test/author/author9.json");
    }

    @Test
    void orcFileWithCompressionTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/orc-file.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFileWithCompressionTest-test",
            "-Pcompression=snappy"
        );

        assertCollectionSize("orcFileWithCompressionTest-test", 5);
    }

    @Test
    void orcFileWithIncorrectCompressionTest() {
        try {
            run(
                "import_orc_files",
                "--path", "src/test/resources/orc-files/orc-file.orc",
                "--clientUri", makeClientUri(),
                "--permissions", DEFAULT_PERMISSIONS,
                "--collections", "orcFileWithCompressionTest-test",
                "-Pcompression=zip"
            );
        } catch(Exception ex){
            assertTrue(ex.getMessage().contains("Codec [zip] is not available. Available codecs are uncompressed, lz4, " +
                "lzo, snappy, zlib, none, zstd."));
        }
    }

    private void verifyDocContent(String uri) {
        JSONDocumentManager documentManager = getDatabaseClient().newJSONDocumentManager();
        String docContent = documentManager.read(uri).next().getContent(new StringHandle()).toString();
        assertTrue(docContent.contains("CitationID"));
        assertTrue(docContent.contains("LastName"));
        assertTrue(docContent.contains("ForeName"));
    }
}
