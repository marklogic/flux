package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AbstractExportFilesCommand extends AbstractExportCommand {

    @Parameter(names = "--path", required = true)
    private String path;

    @Parameter(names = "--partition-by")
    private List<String> partitionBy = new ArrayList<>();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        S3Util.configureAWSCredentialsIfS3Path(session, Arrays.asList(this.path));
//        if (!partitionBy.isEmpty()) {
//            writer.partitionBy(partitionBy.toArray(new String[0]));
//        }
        configureWriter(read(session).write()).mode(getMode()).save(getPath());
        return Optional.empty();
    }

    protected abstract DataFrameWriter configureWriter(DataFrameWriter<Row> writer);

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getPartitionBy() {
        return partitionBy;
    }

    public void setPartitionBy(List<String> partitionBy) {
        this.partitionBy = partitionBy;
    }
}
