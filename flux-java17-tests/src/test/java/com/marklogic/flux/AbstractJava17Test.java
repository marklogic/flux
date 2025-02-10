/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.cli.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractJava17Test extends AbstractFluxTest {

    protected final int run(int expectedReturnCode, String... args) {
        int code = run(args);
        assertEquals(expectedReturnCode, code);
        return code;
    }

    protected final int run(String... args) {
        return Main.run(args);
    }

}
