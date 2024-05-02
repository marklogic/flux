package com.marklogic.newtool.command.custom;

import com.marklogic.newtool.AbstractTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomExportDocumentsTest extends AbstractTest {

    @Test
    void test() {
        run(
            "custom_export_documents",
            "--connectionString", makeConnectionString(),
            "--collections", "author",
            "--target", "marklogic",
            String.format("-P%s=%s", Options.CLIENT_URI, makeConnectionString()),
            String.format("-P%s=%s", Options.WRITE_PERMISSIONS, DEFAULT_PERMISSIONS),
            String.format("-P%s=/exported", Options.WRITE_URI_PREFIX),
            String.format("-P%s=exported-authors", Options.WRITE_COLLECTIONS)
        );

        assertCollectionSize("We can use our own connector as a custom target, and thus we expect the 15 " +
                "author documents to have been written with different URIs to the 'exported-authors' collection.",
            "exported-authors", 15);

        getUrisInCollection("exported-authors", 15)
            .forEach(uri -> assertTrue(uri.startsWith("/exported/author/"), "Unexpected URI: " + uri));
    }
}
