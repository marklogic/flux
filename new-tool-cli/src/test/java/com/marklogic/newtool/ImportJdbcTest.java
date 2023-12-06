package com.marklogic.newtool;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.io.StringHandle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ImportJdbcTest extends AbstractTest {

    /**
     * Demonstrates "simple" joins on language/category that just pull in a single value.
     * Also demonstrates a join that produces an array of values (actor IDs) instead of an array of objects.
     * And demonstrates the "drop" feature which is useful for dropping ID columns that are needed by the query but not
     * desirable to retain in the document.
     */
    @Test
    void filmsWithActorIDs() {
        String query = "select f.*, l.name as language, cat.name as category, fa.actor_id\n" +
            "from film f\n" +
            "inner join public.language l on f.language_id = l.language_id\n" +
            "inner join film_category fc on f.film_id = fc.film_id\n" +
            "inner join category cat on fc.category_id = cat.category_id\n" +
            "inner join film_actor fa on f.film_id = fa.film_id\n" +
            "where f.film_id < 11";

        Main.main(buildArgs(query,
            "--group-by", "film_id",
            "--aggregate", "actor_ids=actor_id",
            "--drop", "language_id", "last_update",

            "--uri-template", "/film/{film_id}.json",
            "--collections", "film,person,organization"
        ));

        JsonNode film = readJsonDocument("/film/1.json", "film");
        assertEquals("English", film.get("language").asText().trim());
        assertEquals("Documentary", film.get("category").asText());
        assertFalse(film.has("language_id"), "language_id should have been dropped");
        assertFalse(film.has("last_update"), "last_update should have been dropped");
        assertEquals(10, film.get("actor_ids").size(), "Expecting 10 actor references; doc: " + film);
    }

    /**
     * Demonstrates a single join that produces an array of payment objects.
     */
    @Test
    void customersWithRentals() {
        String query = "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date\n" +
            "        from customer c\n" +
            "        inner join public.payment p on c.customer_id = p.customer_id\n" +
            "        where c.customer_id = 1";

        Main.main(buildArgs(query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id;amount;payment_date",

            "--uri-template", "/customer/{customer_id}.json",
            "--collections", "customer"
        ));

        JsonNode doc = readJsonDocument("/customer/1.json", "customer");
        assertEquals(1, doc.get("customer_id").asInt());
        assertEquals("Mary", doc.get("first_name").asText());
        assertEquals(30, doc.get("payments").size());

        String json = getDatabaseClient().newTextDocumentManager().read("/customer/1.json", new StringHandle()).get();
        String key = "\"customer_id\"";
        assertEquals(json.indexOf(key), json.lastIndexOf(key), "Should only have one 'customer_id' key in " +
            "the document. Interestingly, MarkLogic will allow for a JSON document to be saved with duplicate keys. " +
            "But when it's retrieved via JacksonHandle, we'll only get one key. So need to retrieve the doc as a " +
            "string so we can verify that 'customer_id' only occurs once.");
    }

    /**
     * Demonstrates multiple aggregations and the apparent need for "array_distinct" to remove duplicate objects from
     * the aggregated arrays.
     */
    @Test
    void customersWithRentalsAndPayments() {
        String query = "select c.customer_id, c.first_name, r.rental_id, r.rental_date, p.payment_id, p.amount\n" +
            "from customer c\n" +
            "inner join public.rental r on c.customer_id = r.customer_id\n" +
            "inner join public.payment p on p.customer_id = p.customer_id\n" +
            "where c.customer_id = 1 and r.rental_id < 1000 and p.payment_id < 17506";

        Main.main(buildArgs(query,
            "--group-by", "customer_id",
            "--aggregate", "payments=payment_id;amount",
            "--aggregate", "rentals=rental_id;rental_date",

            "--uri-template", "/customer/{customer_id}.json",
            "--collections", "customer"
        ));

        JsonNode doc = readJsonDocument("/customer/1.json", "customer");
        assertEquals(3, doc.get("payments").size(), "Customer is expected to have 3 payments " +
            "with IDs of 17503, 17504, and 17505; doc: " + doc);
        assertEquals(2, doc.get("rentals").size(), "Customer is expected to have 2 rentals with " +
            "IDs of 76 and 573; doc: " + doc);
    }


    private String[] buildArgs(String query, String... args) {
        final String[] commonArgs = new String[]{
            "import_jdbc",
            "--jdbc-url", "jdbc:postgresql://localhost/dvdrental",
            "--jdbc-driver", "org.postgresql.Driver",
            "--jdbc-user", "postgres",
            "--jdbc-password", "postgres",
            // See https://stackoverflow.com/a/39129546 for info on an aliased query as a table.
            "--jdbc-table", "(" + query + ") my_query",
        };
        final String[] target = new String[commonArgs.length + args.length];
        System.arraycopy(commonArgs, 0, target, 0, commonArgs.length);
        System.arraycopy(args, 0, target, commonArgs.length, args.length);
//        System.out.println(Arrays.asList(target));
        return target;
    }
}
