package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

/**
 * Can add more tests to this for other commands where we want to ensure that the limit is pushed down
 * for optimization reasons, though that will typically only be verifiable via inspection of logging.
 */
class LimitTest extends AbstractTest {

    private static final String COLLECTION = "limit-files";

    @Test
    void importFiles() {
        importFiles(2);
        assertCollectionSize(COLLECTION, 2);

        importFiles(200);
        assertCollectionSize("Only the 4 matching files should have been selected", COLLECTION, 4);

        importFiles(0);
        assertCollectionSize("Interestingly, asking Spark to apply a limit of zero results in the " +
            "limit being ignored", COLLECTION, 4);
    }

    private void importFiles(int limit) {
        run(
            "import_files",
            "--path", "src/test/resources/mixed-files/hello*",
            "--clientUri", makeClientUri(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", COLLECTION,
            "--limit", Integer.toString(limit)
        );
    }
}
