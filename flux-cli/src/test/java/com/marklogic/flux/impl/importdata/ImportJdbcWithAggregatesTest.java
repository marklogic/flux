/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.io.StringHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportJdbcWithAggregatesTest extends AbstractTest {

    /**
     * Demonstrates a single join that produces an array of payment objects.
     */
    @Test
    void customerWithArrayOfRentals() {
        String query = "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date\n" +
            "        from customer c\n" +
            "        inner join public.payment p on c.customer_id = p.customer_id\n" +
            "        where c.customer_id = 1";

        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id,amount,payment_date",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json"
        );

        JsonNode doc = readJsonDocument("/customer/1.json");
        assertEquals(3, doc.size(), "Expecting 3 fields - customer_id, first_name, and payments");
        assertEquals(1, doc.get("customer_id").asInt());
        assertEquals("Mary", doc.get("first_name").asText());

        ArrayNode payments = (ArrayNode) doc.get("payments");
        assertEquals(30, payments.size(), "Customer 1 has 30 related payments in Postgres.");
        for (int i = 0; i < 30; i++) {
            JsonNode payment = payments.get(i);
            assertEquals(JsonNodeType.NUMBER, payment.get("payment_id").getNodeType());
            assertEquals(JsonNodeType.NUMBER, payment.get("payment_id").getNodeType());
            assertEquals(JsonNodeType.STRING, payment.get("payment_date").getNodeType());
        }

        String json = getDatabaseClient().newTextDocumentManager().read("/customer/1.json", new StringHandle()).get();
        String key = "\"customer_id\"";
        assertEquals(json.indexOf(key), json.lastIndexOf(key), "Should only have one 'customer_id' key in " +
            "the document. Interestingly, MarkLogic will allow for a JSON document to be saved with duplicate keys. " +
            "But when it's retrieved via JacksonHandle, we'll only get one key. So need to retrieve the doc as a " +
            "string so we can verify that 'customer_id' only occurs once.");
    }

    /**
     * Demonstrates a query with 2+ joins, producing a customer document with rentals and payments as
     * separate arrays.
     */
    @Test
    void customerWithArrayOfRentalsAndArrayOfPayments() {
        String query = "select " +
            "c.customer_id, c.first_name, " +
            "r.rental_id, r.inventory_id, " +
            "p.payment_id, p.amount\n" +
            "from customer c\n" +
            "inner join public.rental r on c.customer_id = r.customer_id\n" +
            "inner join public.payment p on p.customer_id = p.customer_id\n" +
            "where c.customer_id = 1 and r.rental_id < 1000 and p.payment_id < 17506";

        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id,amount",
            "--aggregate", "rentals=rental_id,inventory_id",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/customer/{customer_id}.json"
        );

        JsonNode doc = readJsonDocument("/customer/1.json");
        assertEquals(4, doc.size(), "Expecting 4 fields: customer_id, first_name, payments, and rentals");
        assertEquals(1, doc.get("customer_id").asInt());
        assertEquals("Mary", doc.get("first_name").asText());

        ArrayNode payments = (ArrayNode) doc.get("payments");
        assertEquals(3, payments.size(), "The query should have selected 3 related payments.");
        assertEquals(17503, payments.get(0).get("payment_id").asInt());
        assertEquals(7.99, payments.get(0).get("amount").asDouble());
        assertEquals(17504, payments.get(1).get("payment_id").asInt());
        assertEquals(1.99, payments.get(1).get("amount").asDouble());
        assertEquals(17505, payments.get(2).get("payment_id").asInt());
        assertEquals(7.99, payments.get(2).get("amount").asDouble());

        ArrayNode rentals = (ArrayNode) doc.get("rentals");
        assertEquals(2, rentals.size(), "The query should have selected 2 related rentals.");
        assertEquals(76, rentals.get(0).get("rental_id").asInt());
        assertEquals(3021, rentals.get(0).get("inventory_id").asInt());
        assertEquals(573, rentals.get(1).get("rental_id").asInt());
        assertEquals(4020, rentals.get(1).get("inventory_id").asInt());
    }

    /**
     * Demonstrates that a join can produce an array with atomic/simple values, instead of structs / objects.
     */
    @Test
    void joinThatProducesArrayWithAtomicValues() {
        String query = "select f.film_id, f.title, fa.actor_id\n" +
            "from film f\n" +
            "inner join film_actor fa on f.film_id = fa.film_id\n" +
            "where f.film_id = 1";

        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "film_id",
            "--aggregate", "actor_ids=actor_id",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--uri-template", "/film/{film_id}.json"
        );

        JsonNode film = readJsonDocument("/film/1.json");
        assertEquals(3, film.size(), "Expecting 3 fields - film_id, title, and actor_ids");
        assertEquals(1, film.get("film_id").asInt());
        assertEquals(10, film.get("actor_ids").size(), "Expecting 10 actor references to film 1; doc: " + film);
    }

    @Test
    void missingEqualsInAggregationExpression() {
        assertStderrContains(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer",
            "--group-by", "customer_id",
            "--aggregate", "payments,payment_id,amount",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        ), "Invalid aggregation: payments,payment_id,amount; must be of the form " +
            "newColumnName=columnToGroup1,columnToGroup2,etc.");
    }

    @Test
    void badColumnName() {
        String query = "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date\n" +
            "        from customer c\n" +
            "        inner join public.payment p on c.customer_id = p.customer_id\n" +
            "        where c.customer_id < 10";

        assertStderrContains(() -> run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id,notfound",
            "--aggregate", "dates=payment_date",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        ), "Unable to aggregate columns: [payment_id, notfound], [payment_date]; please ensure that each column name " +
            "will be present in the data read from the data source.");
    }
}
