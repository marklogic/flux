package com.marklogic.newtool;

import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

public class AvroTest extends AbstractTest {

    @Test
    void test() {
        SparkSession session = SparkSession.builder()
            .master("local[*]")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();

        final String avroPath = "build/avro";

        session.read()
            .json("src/test/resources/json-lines/*.txt")
            .write()
            .format("avro")
            .mode(SaveMode.Overwrite)
            .save(avroPath);

        session.read()
            .format("avro")
            .load(avroPath)
            .collectAsList()
            .forEach(row -> System.out.println(row.prettyJson()));
    }

}
