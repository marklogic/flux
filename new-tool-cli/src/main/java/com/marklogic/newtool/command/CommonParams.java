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

    @Parameter(names = "--previewVertical", description = "Preview the data in a vertical format instead of in a table.")
    private Boolean previewVertical;

    @Parameter(names = "--repartition", description = "Specify the number of partitions / workers to be used for writing data.")
    private Integer repartition;

    // This is declared here so a user will see it in when viewing command usage, but the Main program does not need
    // it as it can easily check for it in the list of arguments it receives.
    @Parameter(names = "--stacktrace", description = "If included and a command fails, the stacktrace will be printed.")
    private Boolean showStacktrace;

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
        return new Preview(dataset, preview, previewColumnsToDrop, previewVertical != null ? previewVertical.booleanValue() : false);
    }

    public boolean isPreviewRequested() {
        return preview != null;
    }
}
