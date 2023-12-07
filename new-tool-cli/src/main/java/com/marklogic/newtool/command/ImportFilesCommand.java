package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.S3Util;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Import files with a type defined by '--format'")
public class ImportFilesCommand extends AbstractImportCommand {

    @Parameter(names = "--format")
    private String format = "parquet";

    @Parameter(names = "--path", required = true)
    private List<String> paths = new ArrayList<>();

    @Parameter(names = "--schema")
    private String schema;

    @Override
    public void execute(SparkSession session) {
        S3Util.configureAWSCredentialsIfS3Path(session, this.paths);

        write(() -> {
            DataFrameReader reader = session.read()
                .format(format)
                .options(getCustomReadOptions());

            if (schema != null && schema.trim().length() > 0) {
                reader.schema(schema);
            }

            return reader.load(paths.toArray(new String[0]));
        });
    }
}
