package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

public class ImportAvroCommand extends AbstractImportCommand {

    @Parameter(names = "--path", required = true)
    private List<String> paths = new ArrayList<>();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        S3Util.configureAWSCredentialsIfS3Path(session, this.paths);
        // TODO Add avro-specific options.
        return reader.format("avro").load(paths.toArray(new String[0]));
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
