/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.marklogic.flux.impl.Command;
import picocli.CommandLine;

import java.util.Objects;

public abstract class PicoliUtil {

    public static Command getCommandInstance(CommandLine.ParseResult parseResult) {
        CommandLine.ParseResult subcommand = parseResult.subcommand();
        Objects.requireNonNull(subcommand);
        CommandLine.Model.CommandSpec commandSpec = subcommand.commandSpec();
        Objects.requireNonNull(commandSpec);
        return (Command) commandSpec.userObject();
    }

    private PicoliUtil() {
    }
}
