package com.marklogic.newtool;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class S3Util {

    public static void configureAWSCredentialsIfS3Path(SparkSession session, List<String> paths) {
        if (paths.stream().anyMatch(path -> path.startsWith("s3") && !path.startsWith("s3n"))) {
            AWSCredentials creds = new ProfileCredentialsProvider().getCredentials();
            session.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", creds.getAWSAccessKeyId());
            session.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", creds.getAWSSecretKey());
        }

        // See https://sparkbyexamples.com/amazon-aws/write-read-csv-file-from-s3-into-dataframe/
        if (paths.stream().anyMatch(path -> path.startsWith("s3n"))) {
            AWSCredentials creds = new ProfileCredentialsProvider().getCredentials();
            session.sparkContext().hadoopConfiguration().set("fs.s3n.awsAccessKeyId", creds.getAWSAccessKeyId());
            session.sparkContext().hadoopConfiguration().set("fs.s3n.awsSecretAccessKey", creds.getAWSSecretKey());
        }
    }
}
