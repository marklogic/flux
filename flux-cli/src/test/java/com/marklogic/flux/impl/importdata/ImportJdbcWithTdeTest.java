/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.expression.PlanBuilder;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.row.RowManager;
import com.marklogic.client.row.RowRecord;
import com.marklogic.client.row.RowSet;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportJdbcWithTdeTest extends AbstractTest {

    private static final String DEFAULT_TDE_URI = "/tde/junit/customer.json";

    private DatabaseClient schemasDatabaseClient;

    @BeforeEach
    void setup() {
        schemasDatabaseClient = newDatabaseClient("flux-test-schemas");
    }

    @AfterEach
    void deleteCustomerTemplate() {
        // This needs to be deleted after a test so as not to impact other tests that aren't aware that this might
        // exist.
        schemasDatabaseClient.newDocumentManager().delete(DEFAULT_TDE_URI, "/tde/custom-uri.json");
        schemasDatabaseClient.release();
    }

    @Test
    void buildAndLogJsonTde() {
        importJdbcWithArgs(
            "--tde-schema", "demo",
            "--tde-view", "example",
            "--json-root-name", "customer",
            "--tde-preview"
        );

        assertCollectionSize(
            "Just verifying that for now, no documents were imported because a TDE should have been generated and " +
                "logged due to the user of tde-preview.", "customer", 0);
    }

    @Test
    void buildAndLogXmlTde() {
        importJdbcWithArgs(
            "--tde-schema", "demo",
            "--tde-view", "example",
            "--xml-root-name", "customer",
            "--xml-namespace", "http://example.com/customer",
            "--tde-preview"
        );

        assertCollectionSize(
            "Just verifying that for now, no documents were imported because a TDE should have been generated and " +
                "logged due to the user of tde-preview.", "customer", 0);
    }

    @Test
    void tdeWithCollections() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();
    }

    @Test
    void tdeWithDirectories() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-directory", "/customer/",
            // Including a second directory to ensure that it doesn't cause an error.
            "--tde-directory", "/doesnt-matter/"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();
    }

    @Test
    void tdeWithCustomUri() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer",
            "--tde-uri", "/tde/custom-uri.json",

            // Make sure this option works, even though it's set to the default value.
            "--tde-view-layout", "sparse"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();

        assertNotNull(schemasDatabaseClient.newDocumentManager().exists("/tde/custom-uri.json"));
        assertNull(schemasDatabaseClient.newDocumentManager().exists(DEFAULT_TDE_URI));
    }

    @Test
    void tdeCollectionDoesntMatchAnything() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "not-customer"
        );

        assertCollectionSize("customer", 10);

        RowManager rm = getDatabaseClient().newRowManager();
        PlanBuilder op = rm.newPlanBuilder();
        RowSet<RowRecord> rows = rm.resultRows(op.fromView("junit", "customer", ""));
        assertFalse(rows.iterator().hasNext(), "The TDE is valid, but there are no docs in the 'not-customer' " +
            "collection, so no rows should be returned.");
    }

    @Test
    void invalidPermissions() {
        String stderr = runAndReturnStderr(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,

            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role"
        ));

        assertTrue(stderr.contains("Unable to parse permissions string"), "Actual stderr: " + stderr);
        assertCollectionSize(
            "No data should be loaded if an error occurs while loading the TDE template.",
            "customer", 0
        );
    }

    @Test
    void invalidViewLayout() {
        String stderr = runAndReturnStderr(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,

            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-view-layout", "invalid-layout"
        ));

        assertTrue(stderr.contains("TDE-INVALIDTEMPLATENODEVAL"), "Actual stderr: " + stderr);
    }

    private void importJdbcWithArgs(String... options) {
        List<String> defaultArgs = List.of(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json"
        );
        List<String> args = new ArrayList<>(defaultArgs);
        for (String option : options) {
            args.add(option);
        }
        run(args.toArray(new String[0]));
    }

    private void verifyOpticQueryUsingTdeViewReturnsCustomers() {
        RowManager rm = getDatabaseClient().newRowManager();
        PlanBuilder op = rm.newPlanBuilder();
        JsonNode result = rm.resultDoc(op.fromView("junit", "customer", "")
                .select(op.col("customer_id"), op.col("first_name"))
                .orderBy(op.col("customer_id")),
            new JacksonHandle()).get();

        JsonNode rows = result.get("rows");
        assertEquals(10, rows.size());
        assertEquals("Mary", rows.get(0).get("first_name").get("value").asText());
    }
}
