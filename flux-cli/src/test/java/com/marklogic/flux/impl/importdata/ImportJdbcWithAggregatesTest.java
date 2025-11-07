/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.ext.helper.ClientHelper;
import com.marklogic.client.io.StringHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.api.Flux;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportJdbcWithAggregatesTest extends AbstractTest {

    /**
     * Demonstrates a single join that produces an array of payment objects.
     */
    @Test
    void customerWithArrayOfRentals() {
        String query = """
            select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date
            from customer c
            inner join public.payment p on c.customer_id = p.customer_id
            where c.customer_id = 1""";

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
     * separate arrays. Note that aggregate-order-by isn't needed here since a single customer document is being
     * created, and thus the "order by" in the SQL query should work as expected.
     */
    @Test
    void customerWithArrayOfRentalsAndArrayOfPayments() {
        String query = """
            select
            c.customer_id, c.first_name,
            r.rental_id, r.inventory_id,
            p.payment_id, p.amount
            from customer c
            inner join rental r on c.customer_id = r.customer_id
            inner join payment p on c.customer_id = p.customer_id
            where c.customer_id = 1 and r.rental_id < 1000 and p.payment_id < 19000
            order by p.amount
            """;

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
        assertEquals(7, payments.size(), "The query should have selected 7 related payments.");
        assertEquals(0.99, payments.get(0).get("amount").asDouble());
        assertEquals(9.99, payments.get(6).get("amount").asDouble());

        ArrayNode rentals = (ArrayNode) doc.get("rentals");
        assertEquals(2, rentals.size(), "The query should have selected 2 related rentals.");
        assertEquals(76, rentals.get(0).get("rental_id").asInt());
        assertEquals(3021, rentals.get(0).get("inventory_id").asInt());
        assertEquals(573, rentals.get(1).get("rental_id").asInt());
        assertEquals(4020, rentals.get(1).get("inventory_id").asInt());
    }

    /**
     * Verifies that "aggregate-order-by" works for all customers. In this scenario, an "order by" in the SQL query
     * won't work because of how Spark aggregates payments and rentals into many customers.
     */
    @Test
    void orderByPaymentsOnAllCustomers() {
        String query = """
            select
            c.customer_id, c.first_name,
            r.rental_id, r.inventory_id,
            p.payment_id, p.amount
            from customer c
            inner join rental r on c.customer_id = r.customer_id
            inner join payment p on c.customer_id = p.customer_id
            """;

        run(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH, "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id,amount",
            "--aggregate", "rentals=rental_id,inventory_id",
            "--aggregate-order-by", "payments=amount:desc",
            "--aggregate-order-by", "rentals=inventory_id:asc",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "customer"
        );

        verifyEachCustomerHasPaymentsAndRentalsOrdered();
    }

    @Test
    void orderByPaymentsOnAllCustomersWithApi() {
        String query = """
            select
            c.customer_id, c.first_name,
            r.rental_id, r.inventory_id,
            p.payment_id, p.amount
            from customer c
            inner join rental r on c.customer_id = r.customer_id
            inner join payment p on c.customer_id = p.customer_id
            """;

        Flux.importJdbc()
            .from(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .driver(PostgresUtil.DRIVER)
                .query(query)
                .groupBy("customer_id")
                .aggregateColumns("payments", "payment_id", "amount")
                .aggregateColumns("rentals", "rental_id", "inventory_id")
                .aggregateOrderBy("payments", "amount", false)
                .aggregateOrderBy("rentals", "inventory_id", true)
            )
            .connectionString(makeConnectionString())
            .to(options -> options
                .collections("customer")
                .permissionsString(DEFAULT_PERMISSIONS)
            )
            .execute();

        verifyEachCustomerHasPaymentsAndRentalsOrdered();
    }

    /**
     * Demonstrates that a join can produce an array with atomic/simple values, instead of structs / objects.
     */
    @Test
    void joinThatProducesArrayWithAtomicValues() {
        String query = """
            select f.film_id, f.title, fa.actor_id
            from film f
            inner join film_actor fa on f.film_id = fa.film_id
            where f.film_id = 1""";

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
        assertStderrContains(
            "Invalid aggregation: payments,payment_id,amount; must be of the form " +
                "newColumnName=columnToGroup1,columnToGroup2,etc.",
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer",
            "--group-by", "customer_id",
            "--aggregate", "payments,payment_id,amount",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        );
    }

    @Test
    void badColumnName() {
        String query = """
            select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date
            from customer c
            inner join public.payment p on c.customer_id = p.customer_id
            where c.customer_id < 10""";

        assertStderrContains(
            "Unable to aggregate columns: [payment_id, notfound], [payment_date]; please ensure that each column name " +
                "will be present in the data read from the data source.",
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id,notfound",
            "--aggregate", "dates=payment_date",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS
        );
    }

    private void verifyEachCustomerHasPaymentsAndRentalsOrdered() {
        List<String> uris = new ClientHelper(getDatabaseClient()).getUrisInCollection("customer");
        assertTrue(!uris.isEmpty(), "No URIs found in customer collection, something went wrong!");
        for (String uri : uris) {
            JsonNode doc = readJsonDocument(uri);

            // Verify payments are ordered descending by amount
            ArrayNode payments = (ArrayNode) doc.get("payments");
            if (payments.size() >= 2) {
                double firstAmount = payments.get(0).get("amount").asDouble();
                double lastAmount = payments.get(payments.size() - 1).get("amount").asDouble();
                assertTrue(lastAmount <= firstAmount,
                    "Customer doesn't have payments ordered descending! First amount: %f; last amount: %f; doc: %s".formatted(
                        firstAmount, lastAmount, doc.toPrettyString()));
            }

            // Verify rentals are ordered ascending by inventory_id
            ArrayNode rentals = (ArrayNode) doc.get("rentals");
            if (rentals.size() >= 2) {
                int firstInventoryId = rentals.get(0).get("inventory_id").asInt();
                int lastInventoryId = rentals.get(rentals.size() - 1).get("inventory_id").asInt();
                assertTrue(firstInventoryId <= lastInventoryId,
                    "Customer doesn't have rentals ordered ascending! First inventory_id: %d; last inventory_id: %d; doc: %s".formatted(
                        firstInventoryId, lastInventoryId, doc.toPrettyString()));
            }
        }
    }
}
