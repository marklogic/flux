/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private String getConfigValue(String key) {
        Configuration config = new Configuration();
        params.addToHadoopConfiguration(config);
        return config.get(key);
    }
}
