/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.cli.Main;

public abstract class AbstractJava17Test extends AbstractFluxTest {

    protected final int run(String... args) {
        return Main.run(args);
    }

}
