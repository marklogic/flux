/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.spark;

import com.marklogic.flux.cli.Main;
import com.marklogic.flux.impl.Command;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

/**
 * Entry point for spark-submit, where a {@code SparkSession} is expected to exist already and thus does not need
 * to be created.
 */
public class Submit extends Main {

    public static void main(String[] args) {
        CommandLine commandLine = new Submit().newCommandLine();
        if (args.length == 0) {
            commandLine.getErr().println("You must specify a command.");
        } else {
            commandLine.execute(args);
        }
    }

    @Override
    protected SparkSession buildSparkSession(Command selectedCommand) {
        return SparkSession.builder().getOrCreate();
    }
}
