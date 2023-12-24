package com.marklogic.newtool;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import com.marklogic.junit5.AbstractMarkLogicTest;
import org.apache.spark.sql.Row;

import java.util.List;

public abstract class AbstractTest extends AbstractMarkLogicTest {

    private static DatabaseClient databaseClient;

    @Override
    protected DatabaseClient getDatabaseClient() {
        if (databaseClient == null) {
            // Lame, should get host from some properties file.
            databaseClient = new DatabaseClientBuilder()
                .withHost("localhost").withPort(8003)
                .withDigestAuth("new-tool-admin", "password")
                .build();
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

    protected final void run(String... args) {
        Main.main(args);
    }

}
