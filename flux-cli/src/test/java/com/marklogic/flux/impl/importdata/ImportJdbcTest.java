/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
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
            "--total-thread-count", "16",
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
            "--query", "select * from customer",
            "--preview", "10"
        ), "Command failed, cause: Unable to load class: not.valid.driver.value; " +
            "for a JDBC driver, ensure you are specifying the fully-qualified class name for your JDBC driver.");
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
