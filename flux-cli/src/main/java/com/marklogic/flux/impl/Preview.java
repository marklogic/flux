package com.marklogic.flux.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class Preview {

    private final int numberRows;
    private final Dataset<Row> dataset;
    private final List<String> columnsToDrop;
    private final boolean vertical;

    public Preview(Dataset<Row> dataset, int numberRows, List<String> columnsToDrop, boolean vertical) {
        this.dataset = dataset;
        this.numberRows = numberRows;
        this.columnsToDrop = columnsToDrop;
        this.vertical = vertical;
    }

    public void showPreview() {
        Dataset<Row> datasetPreview = dataset;
        if (columnsToDrop != null && !columnsToDrop.isEmpty()) {
            datasetPreview = datasetPreview.drop(columnsToDrop.toArray(new String[]{}));
        }
        // Not truncating at all. For now, users can drop columns if their values are too long.
        datasetPreview.show(numberRows, Integer.MAX_VALUE, vertical);
    }

    public Dataset<Row> getDataset() {
        return dataset;
    }
}
