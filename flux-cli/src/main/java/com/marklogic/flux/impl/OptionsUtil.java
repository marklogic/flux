/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.FluxException;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class OptionsUtil {

    public static final String VALID_VALUES_DESCRIPTION = "Valid values: ${COMPLETION-CANDIDATES} .";

    /**
     * Avoids adding options with a null value, which can cause errors with some Spark data sources.
     *
     * @param keysAndValues sequence of keys and values to construct options
     * @return a map of all keys that have non-null values
     */
    public static Map<String, String> makeOptions(String... keysAndValues) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            String value = keysAndValues[i + 1];
            if (value != null && value.length() > 0) {
                options.put(keysAndValues[i], value);
            }
        }
        return options;
    }

    public static String intOption(int value) {
        return value > 0 ? Integer.toString(value) : null;
    }

    public static Map<String, String> addOptions(Map<String, String> options, String... keysAndValues) {
        options.putAll(makeOptions(keysAndValues));
        return options;
    }

    public static void validateRequiredOptions(Map<String, String> options, String... optionNamesAndMessages) {
        for (int i = 0; i < optionNamesAndMessages.length; i += 2) {
            String key = optionNamesAndMessages[i];
            String value = options.get(key);
            if (value == null || value.trim().length() == 0) {
                throw new FluxException(optionNamesAndMessages[i + 1]);
            }
        }
    }

    /**
     * For use by the CLI.
     *
     * @param parseResult
     * @param options
     */
    public static void verifyHasAtLeastOneOption(CommandLine.ParseResult parseResult, String... options) {
        for (String option : options) {
            if (parseResult.subcommand().hasMatchedOption(option)) {
                return;
            }
        }
        throw new FluxException(String.format(
            "Must specify at least one of the following options: %s.", Arrays.asList(options)));
    }

    private OptionsUtil() {
    }
}
