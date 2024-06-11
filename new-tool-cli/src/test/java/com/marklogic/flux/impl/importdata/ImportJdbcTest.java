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
            "import_jdbc",
            "--jdbcUrl", PostgresUtil.URL,
            "--jdbcUser", PostgresUtil.USER,
            "--jdbcPassword", PostgresUtil.PASSWORD,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uriTemplate", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
    }

    @Test
    void jsonRootName() {
        run(
            "import_jdbc",
            "--jdbcUrl", PostgresUtil.URL,
            "--jdbcUser", PostgresUtil.USER,
            "--jdbcPassword", PostgresUtil.PASSWORD,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--jsonRootName", "customer",
            "--uriTemplate", "/customer/{/customer/customer_id}.json",
            "--collections", "customer"
        );

        JsonNode doc = readJsonDocument("/customer/1.json");
        assertEquals("Mary", doc.get("customer").get("first_name").asText());
    }

    @Test
    void tenCustomersWithUserAndPasswordInUrl() {
        run(
            "import_jdbc",
            "--jdbcUrl", PostgresUtil.URL_WITH_AUTH,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uriTemplate", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
    }

    @Test
    void allCustomers() {
        run(
            "import_jdbc",
            "--jdbcUrl", PostgresUtil.URL_WITH_AUTH,
            "--query", "select * from customer",
            "--connectionString", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "customer",
            "--repartition", "2",
            // Just verifying that these work without causing any errors.
            "--totalThreadCount", "16",
            "--batchSize", "10"
        );

        assertCollectionSize("customer", 599);
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
