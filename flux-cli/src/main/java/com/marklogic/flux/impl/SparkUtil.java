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
        return buildSparkSession("local[*]", false);
    }

    public static SparkSession buildSparkSession(String masterUrl, boolean showConsoleProgress) {
        SparkSession.Builder builder = SparkSession.builder()
            .master(masterUrl)
            .config("spark.sql.session.timeZone", "UTC");

        if (showConsoleProgress) {
            // The main value of this is in showing pixels moving. The info provided by Spark - about tasks and stages -
            // is usually going to be too low-level for a typical user. But because the Spark progress consoles shows
            // an ASCII spinner and some things being updated, it at least gives the user comfort that Flux is not
            // frozen. Note as well that for import, copy, and reprocess commands, the --log-progress feature provides
            // much more useful progress status.
            builder = builder.config("spark.ui.showConsoleProgress", "true");
        }

        return builder.getOrCreate();
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
