package com.marklogic.newtool;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.junit5.AbstractMarkLogicTest;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterEach;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractTest extends AbstractMarkLogicTest {

    private static DatabaseClient databaseClient;

    protected static final String DEFAULT_PERMISSIONS = "new-tool-role,read,new-tool-role,update";

    private SparkSession sparkSession;

    @AfterEach
    public void closeSparkSession() {
        if (sparkSession != null) {
            sparkSession.close();
        }
    }

    @Override
    protected DatabaseClient getDatabaseClient() {
        if (databaseClient == null) {
            Properties props = new Properties();
            try {
                props.load(new ClassPathResource("test.properties").getInputStream());
            } catch (IOException e) {
                fail("Unable to read from test.properties: " + e.getMessage());
            }
            databaseClient = DatabaseClientFactory.newClient(propertyName -> props.get(propertyName));
        }
        return databaseClient;
    }

    @Override
    protected String getJavascriptForDeletingDocumentsBeforeTestRuns() {
        return "declareUpdate(); " +
            "for (var uri of cts.uris(null, [], cts.notQuery(cts.collectionQuery('test-data')))) " +
            "{xdmp.documentDelete(uri)}";
    }

    protected final String makeClientUri() {
        // new-tool-user is expected to have the minimum set of privileges for running all the tests.
        return String.format("%s:%s@%s:%d", "new-tool-user", "password", databaseClient.getHost(), databaseClient.getPort());
    }

    protected final void run(String... args) {
        Main.main(args);
    }

    /**
     * Handy method for running anything and returning everything written to stdout.
     */
    protected final String runAndReturnStdout(Runnable r) {
        PrintStream original = System.out;
        System.out.flush();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PrintStream printStream = new PrintStream(outputStream);
            System.setOut(printStream);
            r.run();
            System.out.flush();
            printStream.flush();
        } finally {
            System.setOut(original);
        }
        return new String(outputStream.toByteArray());
    }

    protected final String runAndReturnStderr(Runnable r) {
        PrintStream original = System.err;
        System.err.flush();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PrintStream printStream = new PrintStream(outputStream);
            System.setErr(printStream);
            r.run();
            System.err.flush();
            printStream.flush();
        } finally {
            System.setErr(original);
        }
        return new String(outputStream.toByteArray());
    }

    /**
     * Useful for when testing the results of a command can be easily done by using our Spark connector.
     *
     * @return
     */
    protected SparkSession newSparkSession() {
        sparkSession = SparkSession.builder()
            .master("local[*]")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();
        return sparkSession;
    }
}
