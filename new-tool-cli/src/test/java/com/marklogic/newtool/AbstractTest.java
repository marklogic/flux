package com.marklogic.newtool;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import com.marklogic.junit5.AbstractMarkLogicTest;
import com.marklogic.newtool.Main;

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

    protected final void run(String... args) {
        Main.main(args);
    }

}
