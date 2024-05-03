package com.marklogic.newtool.command;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.beust.jcommander.Parameter;
import org.apache.hadoop.conf.Configuration;

/**
 * Intended to be a delegate in any command that can access S3.
 */
public class S3Params {

    @Parameter(
        names = "--s3AddCredentials",
        description = "Add credentials retrieved via the AWS SDK to the Spark context for use when accessing S3."
    )
    private boolean addCredentials;

    @Parameter(
        names = "--s3Endpoint",
        description = "Define the S3 endpoint for any operations involving S3; typically used when a " +
            "process like AWS EMR must access an S3 bucket in a separate region."
    )
    private String endpoint;

    /**
     * @param config the Spark runtime configuration object
     */
    public void addToHadoopConfiguration(Configuration config) {
        // See See https://sparkbyexamples.com/amazon-aws/write-read-csv-file-from-s3-into-dataframe/ for more
        // information on the keys used below.
        if (addCredentials) {
            AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
            config.set("fs.s3a.access.key", credentials.getAWSAccessKeyId());
            config.set("fs.s3a.secret.key", credentials.getAWSSecretKey());
            config.set("fs.s3n.awsAccessKeyId", credentials.getAWSAccessKeyId());
            config.set("fs.s3n.awsSecretAccessKey", credentials.getAWSSecretKey());
        }
        if (endpoint != null && endpoint.trim().length() > 0) {
            config.set("fs.s3a.endpoint", endpoint);
            config.set("fs.s3n.endpoint", endpoint);
        }
    }
}
