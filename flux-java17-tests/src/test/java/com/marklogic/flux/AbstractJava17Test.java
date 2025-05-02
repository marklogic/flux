/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.cli.Main;

public abstract class AbstractJava17Test extends AbstractFluxTest {

    protected final int run(String... args) {
        return Main.run(args);
    }

}
