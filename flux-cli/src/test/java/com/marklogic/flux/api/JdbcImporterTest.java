/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcImporterTest extends AbstractTest {

    private static final String QUERY = "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date\n" +
        "from customer c\n" +
        "inner join public.payment p on c.customer_id = p.customer_id\n" +
        "where c.customer_id = 1";

    @Test
    void test() {
        Flux.importJdbc()
            .from(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .driver(PostgresUtil.DRIVER)
                .query(QUERY)
                .groupBy("customer_id")
                .aggregateColumns("payments", "payment_id", "amount", "payment_date"))
            .connectionString(makeConnectionString())
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/customer/{customer_id}.json"))
            .execute();

        JsonNode doc = readJsonDocument("/customer/1.json");
        ArrayNode payments = (ArrayNode) doc.get("payments");
        assertEquals(30, payments.size(), "Customer 1 has 30 related payments in Postgres.");
    }

    @Test
    void withoutDriver() {
        Flux.importJdbc()
            .from(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .driver(PostgresUtil.DRIVER)
                .query("select * from customer"))
            .connectionString(makeConnectionString())
            .limit(10)
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("jdbc-customer"))
            .execute();

        assertCollectionSize("Depending on the JDBC URL, a driver may not need to be specified, which is the " +
            "case for a Postgres JDBC URL.", "jdbc-customer", 10);
    }

    @Test
    void missingJdbcUrl() {
        JdbcImporter importer = Flux.importJdbc()
            .from(options -> options.query(QUERY))
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify a JDBC URL", ex.getMessage());
    }

    @Test
    void missingQuery() {
        JdbcImporter importer = Flux.importJdbc()
            .from(options -> options.url(PostgresUtil.URL))
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Must specify a query", ex.getMessage());
    }
}
