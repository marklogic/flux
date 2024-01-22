package com.marklogic.newtool.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.newtool.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportJdbcTest extends AbstractTest {

    private static final String POSTGRES_PASSWORD = "postgres";

    @Test
    void tenCustomers() {
        run(
            "import_jdbc",
            "--jdbcUrl", "jdbc:postgresql://localhost/dvdrental",
            "--jdbcUser", "postgres",
            "--jdbcPassword", POSTGRES_PASSWORD,
            "--jdbcDriver", "org.postgresql.Driver",
            "--query", "select * from customer where customer_id < 11",
            "--clientUri", makeClientUri(),
            "--uriTemplate", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
    }

    @Test
    void tenCustomersWithUserAndPasswordInUrl() {
        run(
            "import_jdbc",
            "--jdbcUrl", "jdbc:postgresql://localhost/dvdrental?user=postgres&password=" + POSTGRES_PASSWORD,
            "--jdbcDriver", "org.postgresql.Driver",
            "--query", "select * from customer where customer_id < 11",
            "--clientUri", makeClientUri(),
            "--uriTemplate", "/customer/{customer_id}.json",
            "--collections", "customer"
        );

        verifyTenCustomersWereImported();
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
