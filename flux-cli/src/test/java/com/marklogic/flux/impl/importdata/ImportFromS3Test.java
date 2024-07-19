/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.api.Flux;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Intended solely for manual testing of importing files from S3. You'll need to provide your own S3 address and
 * ensure that your AWS credentials are set up locally and correctly. This is not an actual test as we don't yet have a
 * way for Jenkins to authenticate with an S3 bucket.
 */
@Disabled("Only intended for ad hoc testing by using explicit S3 auth values.")
class ImportFromS3Test extends AbstractTest {

    private final static String PATH = "s3a://changeme/";

    @Test
    void test() {
        String stdout = runAndReturnStdout(() -> run(
            "import-files",
            "--path", PATH,
            "--preview", "10",
            "--preview-drop", "content", "modificationTime",
            "--connection-string", makeConnectionString(),
            "--s3-add-credentials"
        ));

        assertNotNull(stdout);
        logger.info("Results: {}", stdout);
    }

    @Test
    void exportTest() {
        String stdout = runAndReturnStdout(() -> run(
            "export-files",
            "--path", PATH,
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--limit", "1",
            "--s3-add-credentials"
        ));

        assertNotNull(stdout);
        logger.info("Results: {}", stdout);
    }

    @Test
    void api() {
        Flux.importGenericFiles()
            .from(options -> options
                .paths(PATH)
                .s3AccessKeyId("changeme")
                .s3SecretAccessKey("changeme"))
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("s3-data"))
            .execute();

        int expectedCount = 15; // Change as needed.
        assertCollectionSize("s3-data", expectedCount);
    }
}
