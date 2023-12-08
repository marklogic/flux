package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExportAvroCommand extends AbstractExportCommand {

    @Parameter(names = "--path", required = true)
    private String path;

    @Parameter(names = "--mode")
    private SaveMode mode = SaveMode.Append;

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        S3Util.configureAWSCredentialsIfS3Path(session, Arrays.asList(this.path));

        read(session)
            .write()
            .format("avro")
            .mode(mode)
            .save(path);
        
        return Optional.empty();
    }
}
