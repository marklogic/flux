/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.cli.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractTest extends AbstractFluxTest {

    protected final int run(int expectedReturnCode, String... args) {
        int code = run(args);
        assertEquals(expectedReturnCode, code);
        return code;
    }

    protected final int run(String... args) {
        return Main.run(args);
    }

    protected final FluxException assertThrowsFluxException(Runnable r) {
        return assertThrows(FluxException.class, r::run);
    }
}
