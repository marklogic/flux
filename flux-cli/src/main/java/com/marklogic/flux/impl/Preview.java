/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.List;

public class Preview {

    @CommandLine.Option(names = "--preview", description = "Show up to the first N records of data read by the command.")
    private int numberRows;

    @CommandLine.Option(names = "--preview-drop", description = "Specify one or more columns to drop when using --preview.", arity = "*")
    private List<String> columnsToDrop;

    @CommandLine.Option(names = "--preview-vertical", description = "Preview the data in a vertical format instead of in a table.")
    private boolean vertical;

    public void showPreview(Dataset<Row> dataset) {
        Dataset<Row> datasetPreview = dataset;
        if (columnsToDrop != null && !columnsToDrop.isEmpty()) {
            datasetPreview = datasetPreview.drop(columnsToDrop.toArray(new String[]{}));
        }
        // Not truncating at all. For now, users can drop columns if their values are too long.
        datasetPreview.show(numberRows, Integer.MAX_VALUE, vertical);
    }

    public int getNumberRows() {
        return numberRows;
    }
}
