package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ImportOrcFilesTest extends AbstractTest {

    @Test
    void orcFileTest() {
        run(
            "import_orc_files",
            "--path", "src/test/resources/orc-files/orc-file.orc",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "orcFile-test"
        );

        assertCollectionSize("orcFile-test", 6000);
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

        assertCollectionSize("orcFileWithCompressionTest-test", 6000);
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
}
