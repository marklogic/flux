/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.marklogic.flux.impl.Command;
import picocli.CommandLine;

import java.util.Objects;

public abstract class PicoliUtil {

    public static Command getCommandInstance(CommandLine.ParseResult parseResult) {
        // Keeps Polaris happy, even though we expect these objects to be non-null via how
        // picoli works.
        Objects.requireNonNull(parseResult);
        Objects.requireNonNull(parseResult.subcommand());
        Objects.requireNonNull(parseResult.subcommand().commandSpec());
        return (Command) parseResult.subcommand().commandSpec().userObject();
    }

    private PicoliUtil() {
    }
}
