/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;
import com.marklogic.junit5.AbstractMarkLogicTest;
import com.marklogic.junit5.XmlNode;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import org.apache.commons.io.IOUtils;
import org.apache.spark.sql.SparkSession;
import org.jdom2.Namespace;
import org.junit.jupiter.api.AfterEach;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractTest extends AbstractMarkLogicTest {

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
            IOUtils.closeQuietly(sparkSession);
        }
        if (!SparkSession.getActiveSession().isEmpty()) {
            IOUtils.closeQuietly(SparkSession.getActiveSession().get());
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
        return "declareUpdate(); " + "for (var uri of cts.uris(null, [], cts.notQuery(cts.collectionQuery('test-data')))) " + "{xdmp.documentDelete(uri)}";
    }

    protected final String makeConnectionString() {
        // flux-test-user is expected to have the minimum set of privileges for running all the tests.
        return String.format("%s:%s@%s:%d", DEFAULT_USER, DEFAULT_PASSWORD, databaseClient.getHost(), databaseClient.getPort());
    }

    /**
     * Useful for when testing the results of a command can be easily done by using our Spark connector.
     *
     * @return
     */
    protected SparkSession newSparkSession() {
        sparkSession = SparkSession.builder().master("local[*]").config("spark.sql.session.timeZone", "UTC").getOrCreate();
        return sparkSession;
    }

    protected final ManageClient newManageClient() {
        Properties props = loadTestProperties();
        ManageConfig config = new ManageConfig(props.getProperty("marklogic.client.host"), 8002, props.getProperty("marklogic.client.username"), props.getProperty("marklogic.client.password"));
        return new ManageClient(config);
    }

    @Override
    protected XmlNode readXmlDocument(String uri) {
        // Registering commonly-used namespaces in tests.
        return super.readXmlDocument(uri,
            Namespace.getNamespace("model", "http://marklogic.com/appservices/model"),
            Namespace.getNamespace("ex", "org:example"),
            Namespace.getNamespace("vec", "http://marklogic.com/vector")
        );
    }

    protected final int run(String... args) {
        return run(null, null, args);
    }

    protected final int run(PrintWriter outWriter, PrintWriter errWriter, String... args) {
        return Main.run(outWriter, errWriter, args);
    }

    // Sonar mistakenly thinks this is production code since it's in src/main/java and thus doesn't like the assert method.
    @SuppressWarnings("java:S5960")
    protected final int run(int expectedReturnCode, String... args) {
        int code = run(args);
        assertEquals(expectedReturnCode, code);
        return code;
    }

    protected final String runAndReturnStdout(String... args) {
        StringWriter stdout = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stdout)) {
            run(writer, null, args);
            writer.flush();
        }
        return stdout.toString();
    }

    protected final String runAndReturnStderr(String... args) {
        return runAndReturnStderr(null, args);
    }

    @SuppressWarnings("java:S5960")
    protected final String runAndReturnStderr(Integer expectedReturnCode, String... args) {
        StringWriter stderr = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stderr)) {
            int actualReturnCode = run(null, writer, args);
            if (expectedReturnCode != null) {
                assertEquals(expectedReturnCode, actualReturnCode);
            }
            writer.flush();
        }
        return stderr.toString();
    }

    protected final void assertStderrContains(String expectedContentInStderr, String... args) {
        assertStderrContains(expectedContentInStderr, null, args);
    }

    @SuppressWarnings("java:S5960")
    protected final void assertStderrContains(String expectedContentInStderr, Integer expectedReturnCode, String... args) {
        final String stderr = runAndReturnStderr(expectedReturnCode, args);
        logger.info("Captured stderr:\n{}", stderr);
        assertTrue(stderr.contains(expectedContentInStderr), String.format("Unexpected stderr; did not find '%s'; actual stderr: %s", expectedContentInStderr, stderr));
    }

    protected final FluxException assertThrowsFluxException(Runnable r) {
        return assertThrows(FluxException.class, r::run);
    }
}
