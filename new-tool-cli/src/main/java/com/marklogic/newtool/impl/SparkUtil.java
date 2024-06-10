package com.marklogic.newtool.impl;

import com.marklogic.newtool.api.SaveMode;
import org.apache.spark.sql.SparkSession;

public class SparkUtil {

    private SparkUtil() {
        // Required by Sonar.
    }

    public static SparkSession buildSparkSession() {
        return buildSparkSession("local[*]");
    }

    public static SparkSession buildSparkSession(String masterUrl) {
        return SparkSession.builder()
            .master(masterUrl)
            // These can be overridden via the "-C" CLI option.
            .config("spark.ui.showConsoleProgress", "true")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();
    }

    /**
     * Converts an instance of our SaveMode enum into Spark's SaveMode. Our SaveMode shields API users from
     * the Spark API and also plays nicely with JCommander's support for enums. JCommander doesn't automatically
     * support the Spark SaveMode enum values since they are upper-camel-case.
     *
     * @param saveMode
     * @return
     */
    public static org.apache.spark.sql.SaveMode toSparkSaveMode(SaveMode saveMode) {
        if (SaveMode.APPEND.equals(saveMode)) {
            return org.apache.spark.sql.SaveMode.Append;
        } else if (SaveMode.ERRORIFEXISTS.equals(saveMode)) {
            return org.apache.spark.sql.SaveMode.ErrorIfExists;
        } else if (SaveMode.IGNORE.equals(saveMode)) {
            return org.apache.spark.sql.SaveMode.Ignore;
        } else if (SaveMode.OVERWRITE.equals(saveMode)) {
            return org.apache.spark.sql.SaveMode.Overwrite;
        }
        return null;
    }
}
