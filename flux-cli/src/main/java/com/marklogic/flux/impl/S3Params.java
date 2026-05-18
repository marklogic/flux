/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.hadoop.conf.Configuration;
import picocli.CommandLine;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Intended to be a delegate in any command that can access S3.
 */
public class S3Params {

    private static final String S3A_CREDENTIALS_PROVIDER = "fs.s3a.aws.credentials.provider";

    @CommandLine.Option(
        names = "--s3-add-credentials",
        description = "Add credentials retrieved via the AWS SDK to the Spark context for use when accessing S3."
    )
    private boolean addCredentials;

    // Added in 2.0.0
    @CommandLine.Option(
        names = "--s3-use-profile",
        description = "Use AWS profile credentials from ~/.aws/config and ~/.aws/credentials files. " +
            "Supports SSO profiles (configured via 'aws sso login'), standard profiles with access keys, " +
            "and any other profile types. Uses the [default] profile or the profile specified by the AWS_PROFILE environment variable."
    )
    private boolean useProfile;

    @CommandLine.Option(
        names = "--s3-anonymous",
        description = "Access S3 without credentials for public buckets that allow anonymous access."
    )
    private boolean anonymousAccess;

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

    @CommandLine.Option(
        names = "--s3-region",
        description = "Specifies the AWS region of the S3 bucket to access."
    )
    private String region;

    /**
     * @param config the Spark runtime configuration object
     */
    public void addToHadoopConfiguration(Configuration config) {
        addToHadoopConfiguration(config, (String) null);
    }

    /**
     * Extracts the bucket from the first S3 path in the list and delegates to
     * {@link #addToHadoopConfiguration(Configuration, String)} so that callers do not need to know about
     * bucket extraction.
     *
     * @param config the Hadoop configuration to modify
     * @param paths  the list of input/output paths; the first S3/S3A/S3N URI in the list is used to determine the bucket
     */
    public void addToHadoopConfiguration(Configuration config, List<String> paths) {
        String bucket = null;
        if (paths != null) {
            for (String path : paths) {
                bucket = extractBucket(path);
                if (bucket != null) {
                    break;
                }
            }
        }
        addToHadoopConfiguration(config, bucket);
    }

    /**
     * Adds S3 credentials and options to the Hadoop configuration. When a bucket name is provided, bucket-scoped
     * keys are used (e.g. {@code fs.s3a.bucket.<name>.access.key}) so that concurrent jobs targeting different
     * buckets do not interfere with each other. When bucket is null, global keys are used as a fallback.
     *
     * @param config the Hadoop configuration to modify
     * @param bucket the S3 bucket name, or null to set global keys
     */
    public void addToHadoopConfiguration(Configuration config, String bucket) {
        // useProfile and anonymousAccess have no bucket-scoped equivalent — always set globally.
        if (anonymousAccess) {
            config.set(S3A_CREDENTIALS_PROVIDER, "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider");
        } else if (useProfile) {
            config.set(S3A_CREDENTIALS_PROVIDER, "software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider");
        }

        String prefix = (bucket != null && !bucket.isEmpty()) ? "fs.s3a.bucket." + bucket + "." : "fs.s3a.";

        if (addCredentials) {
            try (DefaultCredentialsProvider provider = DefaultCredentialsProvider.create()) {
                AwsCredentials credentials = provider.resolveCredentials();
                config.set(prefix + "access.key", credentials.accessKeyId());
                config.set(prefix + "secret.key", credentials.secretAccessKey());
            }
        }

        // See https://sparkbyexamples.com/amazon-aws/write-read-csv-file-from-s3-into-dataframe/ for more
        // information on the keys used below.

        // See https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-providers.html for
        // an explanation of how this provider works. The main use case is for enabling SSO auth.

        if (hasText(accessKeyId)) {
            config.set(prefix + "access.key", accessKeyId);
        }
        if (hasText(secretAccessKey)) {
            config.set(prefix + "secret.key", secretAccessKey);
        }
        if (hasText(sessionToken)) {
            config.set(prefix + "session.token", sessionToken);
        }
        if (hasText(endpoint)) {
            config.set(prefix + "endpoint", endpoint);
        }
        if (hasText(region)) {
            config.set(prefix + "endpoint.region", region);
        }
    }

    /**
     * Extracts the bucket name from an S3 path of the form {@code s3a://bucket/...} or {@code s3://bucket/...}.
     * Returns null if the path is not an S3 URI or the bucket cannot be determined.
     */
    protected static String extractBucket(String path) {
        if (path == null) {
            return null;
        }
        try {
            URI uri = new URI(path);
            String scheme = uri.getScheme();
            if ("s3a".equals(scheme) || "s3".equals(scheme) || "s3n".equals(scheme)) {
                return uri.getHost();
            }
        } catch (URISyntaxException e) {
            // Not a valid URI — fall through to return null
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public void setAddCredentials(boolean addCredentials) {
        this.addCredentials = addCredentials;
    }

    public void setUseProfile(boolean useProfile) {
        this.useProfile = useProfile;
    }

    public void setAnonymousAccess(boolean anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
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

    public void setRegion(String region) {
        this.region = region;
    }
}
