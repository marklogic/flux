/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests for verifying the S3 options. Minio was tried for integration tests, but it didn't work when the
 * tests ran on Jenkins. So depending on this as a smoke test and on manual testing for S3 integration.
 */
class S3ParamsTest {

    private S3Params params = new S3Params();

    @Test
    void useProfile() {
        params.setUseProfile(true);
        assertEquals(
            "software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider",
            getConfigValue("fs.s3a.aws.credentials.provider")
        );
    }

    @Test
    void anonymousAccess() {
        params.setAnonymousAccess(true);
        assertEquals(
            "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider",
            getConfigValue("fs.s3a.aws.credentials.provider")
        );
    }

    @Test
    void anonymousAccessIsAlwaysGlobalEvenWithBucket() {
        params.setAnonymousAccess(true);
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, "my-bucket");
        assertEquals(
            "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider",
            config.get("fs.s3a.aws.credentials.provider")
        );
    }

    @Test
    void anonymousAccessTakesPriorityOverUseProfile() {
        params.setAnonymousAccess(true);
        params.setUseProfile(true);
        assertEquals(
            "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider",
            getConfigValue("fs.s3a.aws.credentials.provider"),
            "Anonymous access should take priority when both options are set"
        );
    }

    @Test
    void accessKeyId() {
        params.setAccessKeyId("my-access-key");
        assertEquals("my-access-key", getConfigValue("fs.s3a.access.key"));
    }

    @Test
    void secretAccessKey() {
        params.setSecretAccessKey("my-secret-key");
        assertEquals("my-secret-key", getConfigValue("fs.s3a.secret.key"));
    }

    @Test
    void sessionToken() {
        params.setSessionToken("my-session-token");
        assertEquals("my-session-token", getConfigValue("fs.s3a.session.token"));
    }

    @Test
    void endpoint() {
        params.setEndpoint("https://s3.us-west-2.amazonaws.com");
        assertEquals("https://s3.us-west-2.amazonaws.com", getConfigValue("fs.s3a.endpoint"));
    }

    @Test
    void region() {
        params.setRegion("us-west-2");
        assertEquals("us-west-2", getConfigValue("fs.s3a.endpoint.region"));
    }

    @Test
    void allOptions() {
        params.setUseProfile(true);
        params.setAccessKeyId("my-access-key");
        params.setSecretAccessKey("my-secret-key");
        params.setSessionToken("my-session-token");
        params.setEndpoint("https://s3.us-west-2.amazonaws.com");
        params.setRegion("us-west-2");

        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config);

        assertEquals(
            "software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider",
            config.get("fs.s3a.aws.credentials.provider")
        );
        assertEquals("my-access-key", config.get("fs.s3a.access.key"));
        assertEquals("my-secret-key", config.get("fs.s3a.secret.key"));
        assertEquals("my-session-token", config.get("fs.s3a.session.token"));
        assertEquals("https://s3.us-west-2.amazonaws.com", config.get("fs.s3a.endpoint"));
        assertEquals("us-west-2", config.get("fs.s3a.endpoint.region"));
    }

    @Test
    void bucketScopedAccessKey() {
        params.setAccessKeyId("my-access-key");
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, "my-bucket");
        assertEquals("my-access-key", config.get("fs.s3a.bucket.my-bucket.access.key"));
        assertNull(config.get("fs.s3a.access.key"), "Global key should not be set when bucket is provided");
    }

    @Test
    void bucketScopedAllCredentials() {
        params.setAccessKeyId("key");
        params.setSecretAccessKey("secret");
        params.setSessionToken("token");
        params.setEndpoint("http://localhost:9000");
        params.setRegion("us-east-1");

        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, "test-bucket");

        assertEquals("key", config.get("fs.s3a.bucket.test-bucket.access.key"));
        assertEquals("secret", config.get("fs.s3a.bucket.test-bucket.secret.key"));
        assertEquals("token", config.get("fs.s3a.bucket.test-bucket.session.token"));
        assertEquals("http://localhost:9000", config.get("fs.s3a.bucket.test-bucket.endpoint"));
        assertEquals("us-east-1", config.get("fs.s3a.bucket.test-bucket.endpoint.region"));
    }

    @Test
    void useProfileIsAlwaysGlobalEvenWithBucket() {
        params.setUseProfile(true);
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, "my-bucket");
        assertEquals(
            "software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider",
            config.get("fs.s3a.aws.credentials.provider")
        );
    }

    @Test
    void extractBucketFromS3aUri() {
        assertEquals("my-bucket", S3Params.extractBucket("s3a://my-bucket/some/prefix/"));
    }

    @Test
    void extractBucketFromS3Uri() {
        assertEquals("my-bucket", S3Params.extractBucket("s3://my-bucket/file.csv"));
    }

    @Test
    void extractBucketFromNonS3Path() {
        assertNull(S3Params.extractBucket("/local/path/file.csv"));
        assertNull(S3Params.extractBucket("abfss://container@account.dfs.core.windows.net/path"));
        assertNull(S3Params.extractBucket(null));
    }

    @Test
    void bucketExtractedFromFirstS3PathInMixedList() {
        params.setAccessKeyId("key");
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, List.of(
            "/local/path/file1.csv",
            "abfss://container@account.dfs.core.windows.net/path",
            "s3a://my-bucket/data/"
        ));
        assertEquals("key", config.get("fs.s3a.bucket.my-bucket.access.key"));
        assertNull(config.get("fs.s3a.access.key"), "Global key should not be set when an S3 path is present");
    }

    @Test
    void addCredentialsWithBucketUsesBucketScopedKeys() {
        params.setAddCredentials(true);
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config, "my-bucket");
        assertNull(config.get("fs.s3a.access.key"), "Global access key should not be set when bucket is provided");
        assertNull(config.get("fs.s3a.secret.key"), "Global secret key should not be set when bucket is provided");
        assertNotNull(config.get("fs.s3a.bucket.my-bucket.access.key"), "Bucket-scoped access key should be set");
        assertNotNull(config.get("fs.s3a.bucket.my-bucket.secret.key"), "Bucket-scoped secret key should be set");
    }

    @Test
    void addCredentialsWithoutBucketUsesGlobalKeys() {
        params.setAddCredentials(true);
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config);
        assertNotNull(config.get("fs.s3a.access.key"), "Global access key should be set when no bucket");
        assertNotNull(config.get("fs.s3a.secret.key"), "Global secret key should be set when no bucket");
    }

    private String getConfigValue(String key) {
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config);
        return config.get(key);
    }
}
