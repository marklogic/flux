/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.cli.Main;

import java.io.PrintWriter;

public abstract class AbstractJava17Test extends AbstractFluxTest {

    @Override
    protected int run(PrintWriter errWriter, String... args) {
        return Main.run(args, errWriter);
    }

}
