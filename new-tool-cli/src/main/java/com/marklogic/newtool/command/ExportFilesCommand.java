package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportFilesCommand extends AbstractExportCommand {

    @Parameter(names = "--format")
    private String format = "parquet";

    @Parameter(names = "--path", required = true)
    private String path;

    @Parameter(names = "--partition-by")
    private List<String> partitionBy = new ArrayList<>();

    @Parameter(names = "--mode")
    private SaveMode mode = SaveMode.Append;

    @Override
    public void execute(SparkSession session) {
        S3Util.configureAWSCredentialsIfS3Path(session, Arrays.asList(this.path));

        DataFrameWriter<Row> writer = read(session).write();
        if (format != null && format.trim().length() > 0) {
            writer.format(format);
        }
        if (!partitionBy.isEmpty()) {
            writer.partitionBy(partitionBy.toArray(new String[0]));
        }
        writer.mode(mode).save(path);
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPartitionBy(List<String> partitionBy) {
        this.partitionBy = partitionBy;
    }

    public void setMode(SaveMode mode) {
        this.mode = mode;
    }
}
