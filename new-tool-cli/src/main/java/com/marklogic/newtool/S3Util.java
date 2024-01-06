package com.marklogic.newtool;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public abstract class S3Util {

    /**
     * If any of the given paths start with "s3", "s3a", or "s3n", the Spark context is modified to include the user's
     * AWS credentials which are retrieved via the AWS SDK.
     * <p>
     * See https://sparkbyexamples.com/amazon-aws/write-read-csv-file-from-s3-into-dataframe/ for more information.
     *
     * @param session
     * @param paths
     */
    public static void configureAWSCredentialsIfS3Path(SparkSession session, List<String> paths) {
        if (paths.stream().anyMatch(S3Util::requiresKeysForS3A)) {
            addAWSCredentialsToSparkContext(session, "fs.s3a.access.key", "fs.s3a.secret.key");
        }
        if (paths.stream().anyMatch(S3Util::requiresKeysForS3N)) {
            addAWSCredentialsToSparkContext(session, "fs.s3n.awsAccessKeyId", "fs.s3n.awsSecretAccessKey");
        }
    }

    private static boolean requiresKeysForS3A(String path) {
        path = path.toLowerCase();
        // Checking "s3" includes both the deprecated s3:// and the preferred s3a://
        return path.startsWith("s3") && !path.startsWith("s3n");
    }

    private static boolean requiresKeysForS3N(String path) {
        return path.toLowerCase().startsWith("s3n");
    }

    private static void addAWSCredentialsToSparkContext(SparkSession session, String accessKeyName, String secretKeyName) {
        AWSCredentials creds = new ProfileCredentialsProvider().getCredentials();
        session.sparkContext().hadoopConfiguration().set(accessKeyName, creds.getAWSAccessKeyId());
        session.sparkContext().hadoopConfiguration().set(secretKeyName, creds.getAWSSecretKey());
    }

    private S3Util() {
    }
}
