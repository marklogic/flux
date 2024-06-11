package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Intended solely for manual testing of importing files from S3. You'll need to provide your own S3 address and
 * ensure that your AWS credentials are set up locally and correctly. This is not an actual test as we don't yet have a
 * way for Jenkins to authenticate with an S3 bucket.
 */
class ImportFromS3Test extends AbstractTest {

    @Disabled("Only intended for ad hoc testing.")
    @Test
    void test() {
        final String path = "s3a://changeme";

        String stdout = runAndReturnStdout(() -> run(
            "import_files",
            "--path", path,
            "--preview", "10",
            "--previewDrop", "content", "modificationTime",
            "--s3AddCredentials"
        ));

        assertNotNull(stdout);
        logger.info("Results: {}", stdout);
    }
}
