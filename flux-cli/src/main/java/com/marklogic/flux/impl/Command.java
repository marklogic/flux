/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

public interface Command {

    /**
     * @param session the Spark session to use for executing the command
     */
    void execute(SparkSession session);

    /**
     * With picocli, we don't have a JCommander-like facility for validating all the params before the command is
     * executed. This allows us to do that.
     *
     * @param parseResult
     */
    void validateCommandLineOptions(CommandLine.ParseResult parseResult);
}
