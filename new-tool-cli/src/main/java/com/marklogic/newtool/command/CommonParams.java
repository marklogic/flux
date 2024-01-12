package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters that are expected to be applicable to any command, regardless of whether MarkLogic the source or the
 * destination of data.
 */
public class CommonParams {

    @Parameter(names = "--limit", description = "Number of rows to read from the data source.")
    private Integer limit;

    @Parameter(names = "--preview", description = "Show up to the first N rows of data read by the command.")
    private Integer preview;

    @Parameter(names = "--previewDrop", description = "Specify one or more columns to drop when using --preview.", variableArity = true)
    private List<String> previewColumnsToDrop = new ArrayList<>();

    public Dataset<Row> applyParams(Dataset<Row> dataset) {
        if (limit != null) {
            dataset = dataset.limit(limit);
        }
        return dataset;
    }

    public Preview makePreview(Dataset<Row> dataset) {
        return new Preview(dataset, preview, previewColumnsToDrop);
    }

    public boolean isPreviewRequested() {
        return preview != null;
    }
}
