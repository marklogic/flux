package com.marklogic.newtool.command;

import java.util.HashMap;
import java.util.Map;

public abstract class OptionsUtil {

    /**
     * Avoids adding options with a null value, which can cause errors with some Spark data sources.
     *
     * @param keysAndValues
     * @return
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

    public static Map<String, String> addOptions(Map<String, String> options, String... keysAndValues) {
        options.putAll(makeOptions(keysAndValues));
        return options;
    }
    
    private OptionsUtil() {
    }
}
