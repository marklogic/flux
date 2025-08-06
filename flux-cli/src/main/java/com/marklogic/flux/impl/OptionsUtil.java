/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.FluxException;

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
            if (value != null && !value.isEmpty()) {
                options.put(keysAndValues[i], value);
            }
        }
        return options;
    }

    public static String intOption(int value) {
        return value > 0 ? Integer.toString(value) : null;
    }

    public static String integerOption(Integer value) {
        return value != null && value > 0 ? value.toString() : null;
    }

    public static Map<String, String> addOptions(Map<String, String> options, String... keysAndValues) {
        options.putAll(makeOptions(keysAndValues));
        return options;
    }

    public static void validateRequiredOptions(Map<String, String> options, String... optionNamesAndMessages) {
        for (int i = 0; i < optionNamesAndMessages.length; i += 2) {
            String key = optionNamesAndMessages[i];
            String value = options.get(key);
            if (value == null || value.trim().isEmpty()) {
                throw new FluxException(optionNamesAndMessages[i + 1]);
            }
        }
    }

    private OptionsUtil() {
    }
}
