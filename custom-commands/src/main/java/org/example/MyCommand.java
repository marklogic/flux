package org.example;

import com.marklogic.etl.api.CustomCommand;
import org.apache.spark.sql.SparkSession;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MyCommand implements CustomCommand {

    @Override
    public void execute(SparkSession session, Map<String, String> dynamicParameters) {
        LoggerFactory.getLogger(getClass()).info("Was able to invoke MyCommand! My params: " + dynamicParameters);
    }
}
