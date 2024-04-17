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
        boolean vertical = false;
        if (previewVertical != null) {
            vertical = previewVertical.booleanValue();
        }
        return new Preview(dataset, preview, previewColumnsToDrop, vertical);
    }

    public boolean isPreviewRequested() {
        return preview != null;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPreview() {
        return preview;
    }

    public void setPreview(Integer preview) {
        this.preview = preview;
    }

    public List<String> getPreviewColumnsToDrop() {
        return previewColumnsToDrop;
    }

    public void setPreviewColumnsToDrop(List<String> previewColumnsToDrop) {
        this.previewColumnsToDrop = previewColumnsToDrop;
    }

    public Boolean getPreviewVertical() {
        return previewVertical;
    }

    public void setPreviewVertical(Boolean previewVertical) {
        this.previewVertical = previewVertical;
    }

    public Integer getRepartition() {
        return repartition;
    }

    public void setRepartition(Integer repartition) {
        this.repartition = repartition;
    }

    public Boolean getShowStacktrace() {
        return showStacktrace;
    }

    public void setShowStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
    }
}
