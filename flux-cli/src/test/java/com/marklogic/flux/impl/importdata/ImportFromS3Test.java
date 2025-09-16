/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.api.Flux;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ImportFromS3Test extends AbstractTest {

    /**
     * This test uses the MinIO instance that is part of the Docker cluster defined in docker-compose.yml. The MinIO
     * instance emulates the S3 API and thus allows for test coverage of most S3 options.
     * <p>
     * This test will keep creating new folders on the MinIO instance hosted by Docker. That's fine - you can easily
     * reset the minio instance by deleting the service and rebuilding the Docker cluster. No teardown method is needed,
     * which would require some AWS SDK code.
     */
    @Test
    void exportThenImport() {
        final String minioPath = "s3a://flux-test-bucket/" + UUID.randomUUID();

        final String minioUser = "minioadmin";
        final String minioPassword = "minioadmin";
        final String minioEndpoint = "http://localhost:8009";

        run(
            "export-files",
            "--path", minioPath,
            "--connection-string", makeConnectionString(),
            "--collections", "author",
            "--partitions-per-forest", "1",
            "--s3-secret-access-key", minioUser,
            "--s3-access-key-id", minioPassword,
            "--s3-endpoint", minioEndpoint,
            "--s3-path-style-access"
        );

        run(
            "import-files",
            "--path", minioPath,
            "--collections", "from-minio",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--s3-secret-access-key", minioUser,
            "--s3-access-key-id", minioPassword,
            "--s3-endpoint", minioEndpoint,
            "--s3-path-style-access"
        );

        assertCollectionSize("from-minio", 15);
    }

    @Test
    @Disabled("Intended for manual testing of a real S3 bucket")
    void api() {
        Flux.importGenericFiles()
            .from(options -> options
                .paths("s3a://changeme/changeme") // Change as needed.
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
