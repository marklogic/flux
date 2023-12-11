package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

public class ImportJsonCommand extends AbstractImportCommand {

    @Parameter(names = "--path", required = true)
    private List<String> paths = new ArrayList<>();

    @Parameter(names = "--jsonLines", description = "Set to true when reading 'JSON Lines' (also called 'newline-delimited') files.")
    private boolean jsonLines;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        S3Util.configureAWSCredentialsIfS3Path(session, this.paths);
        reader = reader.format("json");
        if (!jsonLines) {
            reader.option("multiLine", "true");
        }
        return reader.load(paths.toArray(new String[0]));
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public boolean isJsonLines() {
        return jsonLines;
    }

    public void setJsonLines(boolean jsonLines) {
        this.jsonLines = jsonLines;
    }
}
