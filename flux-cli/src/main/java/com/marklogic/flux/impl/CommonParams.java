/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters that are expected to be applicable to any command, regardless of whether MarkLogic the source or the
 * destination of data.
 */
public class CommonParams {

    @CommandLine.Option(names = "--limit", description = "Number of records to read from the data source.")
    private int limit;

    @CommandLine.Option(names = "--count", description = "Show a count of records to be read from the data source without writing any of the data.")
    private boolean count;

    // picocli doesn't allow a Mixin on an ArgGroup, so we use another ArgGroup here. No need for a heading though as
    // we still want these to show up in the Common Options section in each command's "help" output.
    @CommandLine.ArgGroup(exclusive = false)
    private Preview preview = new Preview();

    @CommandLine.Option(names = "--repartition", description = "Specify the number of partitions to be used for writing data.")
    private int repartition;

    // This is declared here so a user will see it in when viewing command usage, but the Main program does not need
    // it as it can easily check for it in the list of arguments it receives.
    @CommandLine.Option(names = "--stacktrace", description = "Print the stacktrace when a command fails.")
    private boolean showStacktrace;

    @CommandLine.Option(
        names = "--spark-master-url",
        description = "Specify the Spark master URL for configuring the local Spark cluster created by Flux."
    )
    private String sparkMasterUrl;

    @CommandLine.Option(
        names = "-B",
        description = "Specify any key and value to be applied when building the underlying Spark Session; " +
            "%ne e.g. -Bspark.io.encryption.enabled=true . "
    )
    private Map<String, String> sparkSessionBuilderParams = new HashMap<>();

    @CommandLine.Option(
        names = "-C",
        description = "Specify any key and value to be added to the Spark runtime configuration; %ne.g. -Cspark.logConf=true . " +
            "This only accepts Spark configuration options that can be specified after the Spark session has been built."
    )
    private Map<String, String> sparkSessionConfigParams = new HashMap<>();

    public Dataset<Row> applyParams(Dataset<Row> dataset) {
        if (limit > 0) {
            dataset = dataset.limit(limit);
        }
        if (repartition > 0) {
            dataset = dataset.repartition(repartition);
        }
        return dataset;
    }

    public boolean isCount() {
        return count;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setRepartition(int repartition) {
        this.repartition = repartition;
    }

    public String getSparkMasterUrl() {
        return sparkMasterUrl;
    }

    public Map<String, String> getSparkSessionConfigParams() {
        return sparkSessionConfigParams;
    }

    public Map<String, String> getSparkSessionBuilderParams() {
        return sparkSessionBuilderParams;
    }

    public Preview getPreview() {
        return preview;
    }

    public int getRepartition() {
        return repartition;
    }
}
