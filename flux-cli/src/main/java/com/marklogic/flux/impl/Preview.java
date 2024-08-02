/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.spark.Util;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.List;

public class Preview {

    @CommandLine.Option(names = "--preview", description = "Show up to the first N records of data read by the command.")
    private int numberRows;

    @CommandLine.Option(names = "--preview-drop", description = "Specify one or more columns to drop when using --preview.", arity = "*")
    private List<String> columnsToDrop;

    @CommandLine.Option(names = "--preview-list", description = "Preview the data in a list instead of in a table.")
    private boolean displayAsList;

    @CommandLine.Option(names = "--preview-schema", description = "Show the schema of the data read by the command.")
    private boolean showSchema;

    public void showPreview(Dataset<Row> dataset) {
        Dataset<Row> datasetPreview = dataset;
        if (columnsToDrop != null && !columnsToDrop.isEmpty()) {
            datasetPreview = datasetPreview.drop(columnsToDrop.toArray(new String[]{}));
        }
        if (showSchema && Util.MAIN_LOGGER.isInfoEnabled()) {
            Util.MAIN_LOGGER.info("Spark schema of the data read by this command:\n{}", datasetPreview.schema().prettyJson());
        }
        if (numberRows > 0) {
            // Not truncating at all. For now, users can drop columns if their values are too long.
            datasetPreview.show(numberRows, Integer.MAX_VALUE, displayAsList);
        }
    }

    public boolean isPreviewRequested() {
        return numberRows > 0 || showSchema;
    }
}
