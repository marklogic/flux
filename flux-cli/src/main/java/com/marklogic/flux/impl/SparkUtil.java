/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.SaveMode;
import com.marklogic.spark.Util;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

public interface SparkUtil {

    static String makeSparkMasterUrl(int workerThreads) {
        return String.format("local[%d]", workerThreads);
    }

    static SparkSession buildSparkSession() {
        return buildSparkSession(null, null, new HashMap<>());
    }

    static SparkSession buildSparkSession(String masterUrl, SparkConf sparkConf, Map<String, String> sparkSessionBuilderParams) {
        if (sparkConf == null) {
            if (masterUrl == null || masterUrl.trim().isEmpty()) {
                masterUrl = "local[*]";
            }
            if (Util.MAIN_LOGGER.isDebugEnabled()) {
                Util.MAIN_LOGGER.debug("Creating new SparkConf; master URL: {}", masterUrl);
            }
            sparkConf = new SparkConf().setMaster(masterUrl);
        }

        // Gets rid of the SUCCESS files. A Flux user who is not familiar with Spark is likely to be confused by
        // these files. A Flux user experienced with Spark may eventually want this to be configurable, but is
        // also likely to be using spark-submit or directly using the MarkLogic Spark connector, in which case the
        // user has control over this.
        sparkConf.set("spark.hadoop.mapreduce.fileoutputcommitter.marksuccessfuljobs", "false");

        // Spark config options can be provided now or at runtime via spark.conf().set(). The downside to setting
        // options now that are defined by the user is that they won't work when used with spark-submit, which
        // handles constructing a SparkSession. We may eventually provide a feature though for providing options
        // at this point for local users that want more control over the SparkSession itself.
        sparkConf.set("spark.sql.session.timeZone", "UTC");

        // To avoid the need for Jetty on the classpath, which brings in a number of medium CVEs (as Spark 4 is using
        // Jetty 9), the Spark UI is explicitly disabled. Curiously, this doesn't seem to be required - i.e. when
        // excluding org.eclipse.jetty libraries from the classpath, no failures occur. But this is being done to
        // ensure Jetty isn't used to start the Spark UI, as there's currently no use case for that when using Flux.
        sparkConf.set("spark.ui.enabled", "false");

        if (sparkSessionBuilderParams != null) {
            for (Map.Entry<String, String> entry : sparkSessionBuilderParams.entrySet()) {
                sparkConf.set(entry.getKey(), entry.getValue());
            }
        }

        return SparkSession.builder().config(sparkConf).getOrCreate();
    }

    /**
     * Converts an instance of our SaveMode enum into Spark's SaveMode. This was originally created because JCommander
     * could not deal with Spark's SaveMode on account of the values being camel case. Not sure yet if picocli requires
     * this, so it's being left in place.
     *
     * @param saveMode
     * @return
     */
    static org.apache.spark.sql.SaveMode toSparkSaveMode(SaveMode saveMode) {
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

    static Dataset<Row> addFilePathColumn(Dataset<Row> dataset) {
        // The MarkLogic Spark connector has special processing for this column. If it's found by the writer, the
        // value of this column will be used to construct an initial URI for the associated document. The column
        // will then have its value set to null so that the value is not included in the JSON or XML
        // serialization of the row.
        return dataset.withColumn("marklogic_spark_file_path", new Column("_metadata.file_path"));
    }
}
