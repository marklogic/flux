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

    /**
     * Spark configuration options begin with "spark.sql." - for example, see
     * https://spark.apache.org/docs/latest/sql-data-sources-parquet.html . For these to have an effect, they must
     * be included in the Spark configuration instead of as an option for the reader or writer.
     *
     * @param option
     * @return
     */
    public static boolean isSparkConfigurationOption(Map.Entry<String, String> option) {
        return option.getKey() != null && option.getKey().startsWith("spark.sql.");
    }

    /**
     * Any option - typically from a map of dynamic parameters - that does not begin with "spark.sql." is considered
     * to be a data source option that should be included as an option for the reader or writer.
     *
     * @param option
     * @return
     */
    public static boolean isSparkDataSourceOption(Map.Entry<String, String> option) {
        return option.getKey() != null && !option.getKey().startsWith("spark.sql.");
    }

    private OptionsUtil() {
    }
}
