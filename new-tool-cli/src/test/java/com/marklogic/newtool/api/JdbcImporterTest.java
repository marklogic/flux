package com.marklogic.newtool.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marklogic.newtool.AbstractTest;
import com.marklogic.newtool.command.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcImporterTest extends AbstractTest {

    @Test
    void test() {
        String query = "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date\n" +
            "        from customer c\n" +
            "        inner join public.payment p on c.customer_id = p.customer_id\n" +
            "        where c.customer_id = 1";

        NT.importJdbc()
            .readJdbc(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .driver(PostgresUtil.DRIVER)
                .query(query)
                .groupBy("customer_id")
                .aggregationExpressions("payments=payment_id;amount;payment_date"))
            .connectionString(makeConnectionString())
            .writeDocuments(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .uriTemplate("/customer/{customer_id}.json"))
            .execute();

        JsonNode doc = readJsonDocument("/customer/1.json");
        ArrayNode payments = (ArrayNode) doc.get("payments");
        assertEquals(30, payments.size(), "Customer 1 has 30 related payments in Postgres.");
    }
}
