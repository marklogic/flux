/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractTest extends AbstractFluxTest {

    @Override
    protected int run(PrintWriter outWriter, PrintWriter errWriter, String... args) {
        return Main.run(outWriter, errWriter, args);
    }

    protected final FluxException assertThrowsFluxException(Runnable r) {
        return assertThrows(FluxException.class, r::run);
    }
}
