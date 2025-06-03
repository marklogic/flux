/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportJdbcTest extends AbstractTest {

    @Test
    void tenCustomers() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json",
            "--collections", "customer",

            // Including these for manual verification of progress logging.
            "--batch-size", "2",
            "--log-progress", "4"
        );

        verifyTenCustomersWereImported();
    }

    @Test
    void tableInsteadOfQuery() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--table", "customer",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        assertCollectionSize(
            "All customers should be imported (and oddly, the sample dataset has 599 and not 600 customers in it).",
            "customer", 599
        );
    }

    @Test
    void noQueryNorTable() {
        String stderr = runAndReturnStderr(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--connection-string", makeConnectionString()
        ));

        // The formatting of the error is defined by picocli.
        assertTrue(stderr.contains("Missing required argument (specify one of these): (--query <query> | --table <table>)"),
            "Actual stderr: " + stderr);
    }

    @Test
    void jsonRootName() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--json-root-name", "customer",
            "--uri-template", "/customer/{/customer/customer_id}.json",
            "--collections", "customer"
        );

        JsonNode doc = readJsonDocument("/customer/1.json");
        assertEquals("Mary", doc.get("customer").get("first_name").asText());
    }

    @Test
    void tenCustomersWithUserAndPasswordInUrl() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
    }

    @Test
    void allCustomers() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--query", "select * from customer",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "customer",
            "--repartition", "2",
            // Just verifying that these work without causing any errors.
            "--thread-count-per-partition", "8",
            "--batch-size", "10"
        );

        assertCollectionSize("customer", 599);
    }

    @Test
    void badJdbcDriverValue() {
        assertStderrContains(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", "not.valid.driver.value",
            "--connection-string", makeConnectionString(),
            "--query", "select * from customer"
        ), "Error: Unable to load class: not.valid.driver.value; " +
            "for a JDBC driver, ensure you are specifying the fully-qualified class name for your JDBC driver.");
    }

    @Test
    void ignoreNullFields() {
        // First export our author rows, as those have lots of null values.
        run(
            "export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--table", "test_authors",
            "--mode", "overwrite"
        );

        run(
            "import-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", "select * from test_authors",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/null-test/{LastName}.json",
            "--ignore-null-fields"
        );

        JsonNode doc = readJsonDocument("/null-test/Edeler.json");
        assertFalse(doc.has("Base64Value"), "With Base64Value having a null value in the Postgres table and with " +
            "--ignore-null-fields being used, the document in MarkLogic should not have Base64Value.");
    }

    @Test
    void buildAndLogJsonTde() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",

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
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",

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
    void loadTde() {
        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json",

            "--tde-schema", "demo",
            "--tde-view", "example"
        );

        assertCollectionSize("customer", 10);
    }

    private void verifyTenCustomersWereImported() {
        assertCollectionSize("customer", 10);
        JsonNode doc = readJsonDocument("/customer/1.json");

        // Verify a few columns to ensure data was loaded correctly.
        assertEquals(1, doc.get("customer_id").asInt());
        assertEquals(JsonNodeType.NUMBER, doc.get("customer_id").getNodeType());

        assertEquals("Mary", doc.get("first_name").asText());
        assertEquals(JsonNodeType.STRING, doc.get("first_name").getNodeType());

        assertTrue(doc.get("activebool").asBoolean());
        assertEquals(JsonNodeType.BOOLEAN, doc.get("activebool").getNodeType());
    }
}
