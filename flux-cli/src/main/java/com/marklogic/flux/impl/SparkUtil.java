/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.SaveMode;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
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
     * Converts an instance of our SaveMode enum into Spark's SaveMode. This was originally created because JCommander
     * could not deal with Spark's SaveMode on account of the values being camel case. Not sure yet if picocli requires
     * this, so it's being left in place.
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

    public static Dataset<Row> addFilePathColumn(Dataset<Row> dataset) {
        // The MarkLogic Spark connector has special processing for this column. If it's found by the writer, the
        // value of this column will be used to construct an initial URI for the associated document. The column
        // will then have its value set to null so that the value is not included in the JSON or XML
        // serialization of the row.
        return dataset.withColumn("marklogic_spark_file_path", new Column("_metadata.file_path"));
    }
}
