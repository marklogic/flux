/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.marklogic.flux.impl.Command;
import org.apache.spark.sql.SparkSession;

/**
 * Entry point for spark-submit, where a {@code SparkSession} is expected to exist already and thus does not need
 * to be created.
 */
public class Submit extends Main {

    public static void main(String[] args) {
        new Submit().newCommandLine().execute(args);
    }

    @Override
    protected SparkSession buildSparkSession(Command selectedCommand) {
        return SparkSession.builder().getOrCreate();
    }
}
