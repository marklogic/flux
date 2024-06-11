package com.marklogic.flux.impl;

import com.marklogic.flux.cli.Main;

import java.util.Map;

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
        return new Main(args).getSelectedCommand();
    }

}
