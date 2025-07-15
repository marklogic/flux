/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.cli.Main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parent class for unit tests that only verify that args are set as the correct Spark options. Tests that require
 * a Spark session and connectivity to MarkLogic should extend {@code AbstractTest}.
 */
public abstract class AbstractOptionsTest {

    protected final void assertOptions(Map<String, String> options, String... keysAndValues) {
        for (int i = 0; i < keysAndValues.length; i += 2) {
            String key = keysAndValues[i];
            String expectedValue = keysAndValues[i + 1];
            String actualValue = options.get(key);
            assertEquals(expectedValue, actualValue, String.format("Unexpected value %s for key %s", actualValue, key));
        }
    }

    protected final Object getCommand(String... args) {
        AtomicReference<Command> selectedCommand = new AtomicReference<>();
        new Main().newCommandLine()
            .setExecutionStrategy(parseResult -> {
                selectedCommand.set((Command) parseResult.subcommand().commandSpec().userObject());
                return 0;
            })
            .execute(args);
        return selectedCommand.get();
    }

    protected final String applyOptionsAndReturnStderr(String... args) {
        AtomicReference<Command> selectedCommand = new AtomicReference<>();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Main().newCommandLine()
            .setErr(pw)
            .setExecutionStrategy(parseResult -> {
                selectedCommand.set((Command) parseResult.subcommand().commandSpec().userObject());
                return 0;
            })
            .execute(args);
        pw.flush();
        return sw.toString();
    }

}
