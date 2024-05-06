package com.marklogic.newtool;

import org.apache.spark.sql.SparkSession;

public class SparkUtil {

    private SparkUtil() {
        // Required by Sonar.
    }
    
    public static SparkSession buildSparkSession() {
        // Will make these hardcoded strings configurable soon.
        return SparkSession.builder()
            .master("local[*]")
            .config("spark.ui.showConsoleProgress", "true")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();
    }
}
