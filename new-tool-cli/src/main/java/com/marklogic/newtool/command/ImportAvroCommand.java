package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportAvroCommand extends AbstractImportCommand {

    @Parameter(names = "--path", required = true)
    private List<String> paths = new ArrayList<>();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        S3Util.configureAWSCredentialsIfS3Path(session, this.paths);

        DataFrameReader reader = session.read().format("avro");
        return write(() -> reader
            .options(getCustomReadOptions())
            .load(paths.toArray(new String[0])));
    }
}
