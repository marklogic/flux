package com.marklogic.etl.api;

import org.apache.spark.sql.SparkSession;

import java.util.Map;

public interface CustomCommand {

    void execute(SparkSession session, Map<String, String> dynamicParameters);
}
