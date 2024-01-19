package com.marklogic.newtool;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.junit5.AbstractMarkLogicTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractTest extends AbstractMarkLogicTest {

    private static DatabaseClient databaseClient;

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
        PrintStream originalStdout = System.out;
        System.out.flush();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            System.setOut(printStream);
            r.run();
            System.out.flush();
            return new String(outputStream.toByteArray());
        } finally {
            System.setOut(originalStdout);
        }
    }
}
