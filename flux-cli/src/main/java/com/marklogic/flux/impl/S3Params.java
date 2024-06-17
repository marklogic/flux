/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.beust.jcommander.Parameter;
import org.apache.hadoop.conf.Configuration;

/**
 * Intended to be a delegate in any command that can access S3.
 */
public class S3Params {

    private static final String S3A_ACCESS_KEY = "fs.s3a.access.key";
    private static final String S3A_SECRET_KEY = "fs.s3a.secret.key";
    private static final String S3N_ACCESS_KEY = "fs.s3n.awsAccessKeyId";
    private static final String S3N_SECRET_KEY = "fs.s3n.awsSecretAccessKey";

    @Parameter(
        names = "--s3-add-credentials",
        description = "Add credentials retrieved via the AWS SDK to the Spark context for use when accessing S3."
    )
    private boolean addCredentials;

    @Parameter(
        names = "--s3-access-key-id",
        description = "Specifies the AWS access key ID to use for accessing S3 paths."
    )
    private String accessKeyId;

    @Parameter(
        names = "--s3-secret-access-key",
        description = "Specifies the AWS secret key to use for accessing S3 paths."
    )
    private String secretAccessKey;

    @Parameter(
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
            AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
            config.set(S3A_ACCESS_KEY, credentials.getAWSAccessKeyId());
            config.set(S3A_SECRET_KEY, credentials.getAWSSecretKey());
            config.set(S3N_ACCESS_KEY, credentials.getAWSAccessKeyId());
            config.set(S3N_SECRET_KEY, credentials.getAWSSecretKey());
        }
        if (accessKeyId != null && accessKeyId.trim().length() > 0) {
            config.set(S3A_ACCESS_KEY, accessKeyId);
            config.set(S3N_ACCESS_KEY, accessKeyId);
        }
        if (secretAccessKey != null && secretAccessKey.trim().length() > 0) {
            config.set(S3A_SECRET_KEY, secretAccessKey);
            config.set(S3N_SECRET_KEY, secretAccessKey);
        }
        if (endpoint != null && endpoint.trim().length() > 0) {
            config.set("fs.s3a.endpoint", endpoint);
            config.set("fs.s3n.endpoint", endpoint);
        }
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

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
