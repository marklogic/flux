package com.marklogic.newtool.cli;

import org.apache.spark.sql.SparkSession;

/**
 * Entry point for spark-submit, where a {@code SparkSession} is expected to exist already and thus does not need
 * to be created.
 */
public class Submit extends Main {

    public Submit(String... args) {
        super(args);
    }

    public static void main(String[] args) {
        new Submit(args).run();
    }

    @Override
    protected SparkSession buildSparkSession() {
        return SparkSession.builder().getOrCreate();
    }
}
