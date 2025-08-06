/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.cli.Main;

import java.io.PrintWriter;

public abstract class AbstractJava17Test extends AbstractFluxTest {

    @Override
    protected int run(PrintWriter outWriter, PrintWriter errWriter, String... args) {
        return Main.run(outWriter, errWriter, args);
    }

}
