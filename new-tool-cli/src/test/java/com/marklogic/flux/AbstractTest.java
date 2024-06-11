package com.marklogic.flux;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.junit5.AbstractMarkLogicTest;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;
import com.marklogic.rest.util.RestTemplateUtil;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractTest extends AbstractMarkLogicTest {

    private static DatabaseClient databaseClient;

    protected static final String DEFAULT_PERMISSIONS = "new-tool-role,read,new-tool-role,update";

    protected static final String READ_AUTHORS_OPTIC_QUERY = "op.fromView('Medical', 'Authors', '')";

    protected static final String DEFAULT_USER = "new-tool-user";
    protected static final String DEFAULT_PASSWORD = "password";

    private SparkSession sparkSession;

    @AfterEach
    public void closeSparkSession() {
        if (sparkSession != null) {
            sparkSession.close();
        }
        if (!SparkSession.getActiveSession().isEmpty()) {
            SparkSession.getActiveSession().get().close();
        }
    }

    @Override
    protected DatabaseClient getDatabaseClient() {
        if (databaseClient == null) {
            databaseClient = newDatabaseClient(null);
        }
        return databaseClient;
    }

    protected final DatabaseClient newDatabaseClient(String database) {
        Properties props = loadTestProperties();
        if (database != null) {
            props.setProperty("marklogic.client.database", database);
        }
        return DatabaseClientFactory.newClient(propertyName -> props.get(propertyName));
    }

    protected final Properties loadTestProperties() {
        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("test.properties").getInputStream());
        } catch (IOException e) {
            fail("Unable to read from test.properties: " + e.getMessage());
        }
        return props;
    }

    @Override
    protected String getJavascriptForDeletingDocumentsBeforeTestRuns() {
        return "declareUpdate(); " +
            "for (var uri of cts.uris(null, [], cts.notQuery(cts.collectionQuery('test-data')))) " +
            "{xdmp.documentDelete(uri)}";
    }

    protected final String makeConnectionString() {
        // new-tool-user is expected to have the minimum set of privileges for running all the tests.
        return String.format("%s:%s@%s:%d", DEFAULT_USER, DEFAULT_PASSWORD, databaseClient.getHost(), databaseClient.getPort());
    }

    protected final void run(String... args) {
        Main.main(args);
    }

    /**
     * Handy method for running anything and returning everything written to stdout - except that this is
     * proving to be very fragile, as sometimes nothing gets written to the byte stream. An individual test that uses
     * this will work fine, but then it may fail when run in the entire suite. Not yet known why.
     */
    protected final String runAndReturnStdout(Runnable r) {
        System.out.flush();
        PrintStream original = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outputStream, true));
            r.run();
        } finally {
            System.out.flush();
            System.setOut(original);
        }
        return new String(outputStream.toByteArray());
    }

    protected final String runAndReturnStderr(Runnable r) {
        System.err.flush();
        PrintStream original = System.err;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(outputStream, true));
            r.run();
        } finally {
            System.err.flush();
            System.setErr(original);
        }
        return new String(outputStream.toByteArray());
    }

    protected final void assertStderrContains(Runnable r, String expectedContentInStderr) {
        final String stderr = runAndReturnStderr(r);
        logger.info("Captured stderr:\n{}", stderr);
        assertTrue(
            stderr.contains(expectedContentInStderr),
            String.format("Unexpected stderr; did not find '%s'; actual stderr: %s", expectedContentInStderr, stderr)
        );
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

    protected final FluxException assertThrowsNtException(Runnable r) {
        return assertThrows(FluxException.class, () -> r.run());
    }

    protected final ManageClient newManageClient() {
        Properties props = loadTestProperties();
        // Forcing usage of the deprecated Apache HttpClient to avoid classpath issues with the relocated OkHttp classes
        // in our connector jar.
        ManageConfig manageConfig = new ManageConfig(props.getProperty("marklogic.client.host"), 8002,
            props.getProperty("marklogic.client.username"),
            props.getProperty("marklogic.client.password")
        );
        RestTemplate restTemplate = RestTemplateUtil.newRestTemplate(manageConfig, RestTemplateUtil.DEFAULT_CONFIGURERS);
        ManageClient manageClient = new ManageClient(restTemplate);
        manageClient.setManageConfig(manageConfig);
        return manageClient;
    }
}
