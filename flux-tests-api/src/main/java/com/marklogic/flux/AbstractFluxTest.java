/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.junit5.AbstractMarkLogicTest;
import com.marklogic.junit5.XmlNode;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.rest.util.MgmtResponseErrorHandler;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.spark.sql.SparkSession;
import org.jdom2.Namespace;
import org.junit.jupiter.api.AfterEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractFluxTest extends AbstractMarkLogicTest {

    private DatabaseClient databaseClient;

    protected static final String DEFAULT_PERMISSIONS = "flux-test-role,read,flux-test-role,update";

    protected static final String READ_AUTHORS_OPTIC_QUERY = "op.fromView('Medical', 'Authors', '')";

    protected static final String DEFAULT_USER = "flux-test-user";
    protected static final String DEFAULT_PASSWORD = "password";

    protected static final String DEFAULT_MARKLOGIC_GRAPH = "http://marklogic.com/semantics#default-graph";

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
        return DatabaseClientFactory.newClient(props::get);
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

    /**
     * Handy method for running anything and returning everything written to stdout - except that this is
     * proving to be very fragile, as sometimes nothing gets written to the byte stream. An individual test that uses
     * this will work fine, but then it may fail when run in the entire suite. Not yet known why.
     */
    // Telling Sonar not to worry about System.out usage.
    @SuppressWarnings("java:S106")
    protected final String runAndReturnStdout(Runnable r) {
        System.out.flush();
        PrintStream original = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(outputStream, true, Charset.defaultCharset())) {
            System.setOut(ps);
            r.run();
        } finally {
            System.out.flush();
            System.setOut(original);
        }
        String stdout = new String(outputStream.toByteArray(), Charset.defaultCharset());
        logger.info("Captured stdout:\n{}", stdout);
        return stdout;
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

    @Override
    protected XmlNode readXmlDocument(String uri) {
        // Registering commonly-used namespaces in tests.
        return super.readXmlDocument(uri,
            Namespace.getNamespace("model", "http://marklogic.com/appservices/model"),
            Namespace.getNamespace("ex", "org:example")
        );
    }

    protected final int run(String... args) {
        return run(null, args);
    }

    protected abstract int run(PrintWriter errWriter, String... args);

    // Sonar mistakenly thinks this is production code since it's in src/main/java and thus doesn't like the assert method.
    @SuppressWarnings("java:S5960")
    protected final int run(int expectedReturnCode, String... args) {
        int code = run(args);
        assertEquals(expectedReturnCode, code);
        return code;
    }

    protected final String runAndReturnStderr(String... args) {
        return runAndReturnStderr(null, args);
    }

    @SuppressWarnings("java:S5960")
    protected final String runAndReturnStderr(Integer expectedReturnCode, String... args) {
        StringWriter stderr = new StringWriter();
        PrintWriter writer = new PrintWriter(stderr);
        int actualReturnCode = run(writer, args);
        if (expectedReturnCode != null) {
            assertEquals(expectedReturnCode, actualReturnCode);
        }
        writer.flush();
        return stderr.toString();
    }

    protected final void assertStderrContains(String expectedContentInStderr, String... args) {
        assertStderrContains(expectedContentInStderr, null, args);
    }

    @SuppressWarnings("java:S5960")
    protected final void assertStderrContains(String expectedContentInStderr, Integer expectedReturnCode, String... args) {
        final String stderr = runAndReturnStderr(expectedReturnCode, args);
        logger.info("Captured stderr:\n{}", stderr);
        assertTrue(
            stderr.contains(expectedContentInStderr),
            String.format("Unexpected stderr; did not find '%s'; actual stderr: %s", expectedContentInStderr, stderr)
        );
    }
}
