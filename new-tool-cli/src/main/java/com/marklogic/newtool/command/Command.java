package com.marklogic.newtool.command;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Optional;

public interface Command {

    /**
     * @param session
     * @return Some commands may support a "preview" that returns the rows that were read without passing them to
     * the writer. Those commands can return a list of rows.
     */
    Optional<List<Row>> execute(SparkSession session);
}
