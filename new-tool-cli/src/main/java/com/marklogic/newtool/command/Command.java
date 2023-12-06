package com.marklogic.newtool.command;

import org.apache.spark.sql.SparkSession;

public interface Command {

    void execute(SparkSession session);
}
