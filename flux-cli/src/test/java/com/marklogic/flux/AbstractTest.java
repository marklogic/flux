/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;
import com.marklogic.junit5.AbstractMarkLogicTest;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.rest.util.MgmtResponseErrorHandler;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    protected static final String DEFAULT_PERMISSIONS = "flux-test-role,read,flux-test-role,update";

    protected static final String READ_AUTHORS_OPTIC_QUERY = "op.fromView('Medical', 'Authors', '')";

    protected static final String DEFAULT_USER = "flux-test-user";
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
        // flux-test-user is expected to have the minimum set of privileges for running all the tests.
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
        String stdout = new String(outputStream.toByteArray());
        logger.info("Captured stdout:\n{}", stdout);
        return stdout;
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

    protected final FluxException assertThrowsFluxException(Runnable r) {
        return assertThrows(FluxException.class, () -> r.run());
    }

    /**
     * Constructs a ManageClient using the "old" Apache HttpClient approach. This avoids classpath issues due to the
     * relocated OkHttp classes in the connector jar. This code was copied from ml-gradle before its 5.0.0 release;
     * it was removed from the 5.0.0 release as it had been deprecated for a while.
     *
     * @return
     */
    protected final ManageClient newManageClient() {
        Properties props = loadTestProperties();
        // Forcing usage of the deprecated Apache HttpClient to avoid classpath issues with the relocated OkHttp classes
        // in our connector jar.
        ManageConfig config = new ManageConfig(props.getProperty("marklogic.client.host"), 8002,
            props.getProperty("marklogic.client.username"),
            props.getProperty("marklogic.client.password")
        );

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(config.getHost(), config.getPort(), AuthScope.ANY_REALM),
            new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
        httpClientBuilder.setDefaultCredentialsProvider(provider);

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build()));
        restTemplate.setErrorHandler(new MgmtResponseErrorHandler());
        ManageClient manageClient = new ManageClient(restTemplate);
        manageClient.setManageConfig(config);
        return manageClient;
    }
}
