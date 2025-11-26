/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.hadoop.conf.Configuration;
import picocli.CommandLine;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

/**
 * Intended to be a delegate in any command that can access S3.
 */
public class S3Params {

    private static final String S3A_ACCESS_KEY = "fs.s3a.access.key";
    private static final String S3A_SECRET_KEY = "fs.s3a.secret.key";

    @CommandLine.Option(
        names = "--s3-add-credentials",
        description = "Add credentials retrieved via the AWS SDK to the Spark context for use when accessing S3."
    )
    private boolean addCredentials;

    @CommandLine.Option(
        names = "--s3-access-key-id",
        description = "Specifies the AWS access key ID to use for accessing S3 paths."
    )
    private String accessKeyId;

    @CommandLine.Option(
        names = "--s3-secret-access-key",
        description = "Specifies the AWS secret key to use for accessing S3 paths."
    )
    private String secretAccessKey;

    @CommandLine.Option(
        names = "--s3-session-token",
        description = "Specifies the AWS session token to use, along with the access key ID and secret access key, for accessing S3 paths."
    )
    private String sessionToken;

    @CommandLine.Option(
        names = "--s3-endpoint",
        description = "Define the S3 endpoint for any operations involving S3; typically used when a " +
            "process like AWS EMR must access an S3 bucket in a separate region."
    )
    private String endpoint;

    /**
     * @param config the Spark runtime configuration object
     */
    public void addToHadoopConfiguration(Configuration config) {
        // See https://sparkbyexamples.com/amazon-aws/write-read-csv-file-from-s3-into-dataframe/ for more
        // information on the keys used below.
        if (addCredentials) {
            try (DefaultCredentialsProvider provider = DefaultCredentialsProvider.create()) {
                AwsCredentials credentials = provider.resolveCredentials();
                config.set(S3A_ACCESS_KEY, credentials.accessKeyId());
                config.set(S3A_SECRET_KEY, credentials.secretAccessKey());
            }
        }

        if (hasText(accessKeyId)) {
            config.set(S3A_ACCESS_KEY, accessKeyId);
        }
        if (hasText(secretAccessKey)) {
            config.set(S3A_SECRET_KEY, secretAccessKey);
        }
        if (hasText(sessionToken)) {
            config.set("fs.s3a.session.token", sessionToken);
        }
        if (hasText(endpoint)) {
            config.set("fs.s3a.endpoint", endpoint);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public void setAddCredentials(boolean addCredentials) {
        this.addCredentials = addCredentials;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
