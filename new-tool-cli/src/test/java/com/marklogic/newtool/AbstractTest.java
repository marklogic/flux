package com.marklogic.newtool;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.junit5.AbstractMarkLogicTest;
import org.apache.spark.sql.Row;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
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

    protected final void logRows(List<Row> rows) {
        rows.forEach(row -> logger.info(row.prettyJson()));
    }

    // TODO Add test for this, and change it to return a Dataset instead of a List which may be too large for memory.
    protected final List<Row> preview(String... args) {
        String[] finalArgs = new String[args.length + 1];
        System.arraycopy(args, 0, finalArgs, 0, args.length);
        finalArgs[args.length] = "--preview";
        return new Main(finalArgs).run().get();
    }

    protected final String makeClientUri() {
        // new-tool-user is expected to have the minimum set of privileges for running all the tests.
        return String.format("%s:%s@%s:%d", "new-tool-user", "password", databaseClient.getHost(), databaseClient.getPort());
    }

    protected final void run(String... args) {
        Main.main(args);
    }

}
