package com.marklogic.newtool;

import com.marklogic.etl.api.CustomCommand;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

public class TestCustomCommand implements CustomCommand {

    private static Map<String, String> dynamicParameters;

    /**
     * Doesn't do anything with the Spark session yet, it's just verifying that the params get passed in correctly.
     */
    @Override
    public void execute(SparkSession session, Map<String, String> dynamicParameters) {
        this.dynamicParameters = dynamicParameters;
    }

    public static Map<String, String> getDynamicParameters() {
        return dynamicParameters;
    }
}
