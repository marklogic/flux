/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.Optional;

public interface Command {

    /**
     * @param session the Spark session to use for executing the command
     * @return Some commands may support a "preview" that returns the rows that were read without passing them to
     * the writer. Those commands can return a preview of the dataset that has been read by the command.
     */
    Optional<Preview> execute(SparkSession session);

    /**
     * With picocli, we don't have a JCommander-like facility for validating all the params before the command is
     * executed. This allows us to do that.
     *
     * @param parseResult
     */
    void validateCommandLineOptions(CommandLine.ParseResult parseResult);
}
