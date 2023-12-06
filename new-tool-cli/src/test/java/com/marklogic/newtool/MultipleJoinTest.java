package com.marklogic.newtool;

import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

public class MultipleJoinTest {

    /**
     * Just hardcoding in here for now until we figure out how to solve this problem.
     */
    @Test
    void test() {
        String query = "select c.customer_id, c.first_name, r.rental_id, r.rental_date, p.payment_id, p.amount\n" +
            "from customer c\n" +
            "inner join public.rental r on c.customer_id = r.customer_id\n" +
            "inner join public.payment p on p.customer_id = p.customer_id\n" +
            "where c.customer_id < 2 and r.rental_id < 1000 and p.payment_id < 17506";

        String jdbcTable = String.format("(%s) myQuery", query);

        Properties jdbcProps = new Properties();
        jdbcProps.setProperty("driver", "org.postgresql.Driver");
        jdbcProps.setProperty("user", "postgres");
        jdbcProps.setProperty("password", "postgres");

        SparkSession session = SparkSession.builder()
            .master("local[*]")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();

        Dataset<Row> dataset = session.read().jdbc("jdbc:postgresql://localhost/dvdrental", jdbcTable, jdbcProps);

        // Prints the original 6 rows
        List<Row> rows = dataset.collectAsList();
        rows.forEach(row -> System.out.println(row.json()));

        // Try to aggregate both payments and rentals.
        // Doesn't work - we end up with 6 rentals and 6 payments, on account of there being 2 unique rentals and
        // 3 unique payments.
        dataset = dataset.groupBy("customer_id").agg(
            functions.first("first_name").alias("first_name"),
            functions.array_distinct(functions.collect_list(functions.struct("rental_id", "rental_date"))).alias("rentals"),
            functions.array_distinct(functions.collect_list(functions.struct("payment_id", "amount"))).alias("payments")
        );

        // Print the results
        rows = dataset.collectAsList();
        rows.forEach(row -> System.out.println(row.prettyJson()));
    }
}
