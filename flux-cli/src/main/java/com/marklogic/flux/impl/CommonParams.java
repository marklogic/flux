/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameters that are expected to be applicable to any command, regardless of whether MarkLogic the source or the
 * destination of data.
 */
public class CommonParams {

    @CommandLine.Option(names = "--limit", description = "Number of records to read from the data source.")
    private Integer limit;

    @CommandLine.Option(names = "--count", description = "Show a count of records to be read from the data source without writing any of the data.")
    private boolean count;

    @CommandLine.Option(names = "--preview", description = "Show up to the first N records of data read by the command.")
    private Integer preview;

    @CommandLine.Option(names = "--preview-drop", description = "Specify one or more columns to drop when using --preview.", arity = "*")
    private List<String> previewColumnsToDrop = new ArrayList<>();

    @CommandLine.Option(names = "--preview-vertical", description = "Preview the data in a vertical format instead of in a table.")
    private boolean previewVertical;

    @CommandLine.Option(names = "--repartition", description = "Specify the number of partitions to be used for writing data.")
    private Integer repartition;

    // This is declared here so a user will see it in when viewing command usage, but the Main program does not need
    // it as it can easily check for it in the list of arguments it receives.
    @CommandLine.Option(names = "--stacktrace", description = "Print the stacktrace when a command fails.")
    private Boolean showStacktrace;

    // Hidden for now since showing it for every command in its "help" seems confusing for most users that will likely
    // never need to know about this.
    @CommandLine.Option(
        names = "--master-url",
        description = "Specify the Spark master URL for configuring the local Spark cluster created by Flux."
    )
    private String sparkMasterUrl = "local[*]";

    @CommandLine.Option(
        names = "-C",
        description = "Specify any key and value to be added to the Spark runtime configuration; %ne.g. -Cspark.logConf=true."
    )
    private Map<String, String> configParams = new HashMap<>();

    public Dataset<Row> applyParams(Dataset<Row> dataset) {
        if (limit != null) {
            dataset = dataset.limit(limit);
        }
        if (repartition != null && repartition > 0) {
            dataset = dataset.repartition(repartition);
        }
        return dataset;
    }

    public Preview makePreview(Dataset<Row> dataset) {
        return new Preview(dataset, preview, previewColumnsToDrop, previewVertical);
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public boolean isCount() {
        return count;
    }

    public void setPreview(Integer preview) {
        this.preview = preview;
    }

    public boolean isPreviewRequested() {
        return preview != null;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setRepartition(Integer repartition) {
        this.repartition = repartition;
    }

    public String getSparkMasterUrl() {
        return sparkMasterUrl;
    }

    public Map<String, String> getConfigParams() {
        return configParams;
    }
}
