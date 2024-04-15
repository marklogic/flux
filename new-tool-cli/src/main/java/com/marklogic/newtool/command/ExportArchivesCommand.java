package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

public class ExportArchivesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private ReadDocumentParams readDocumentParams = new ReadDocumentParams();

    @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of data to include. " +
        "Valid choices are: content, metadata (for all types of metadata), collections, permissions, quality, properties, and metadatavalues.")
    private String categories = "content,metadata";

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(OptionsUtil.makeOptions(Options.READ_DOCUMENTS_CATEGORIES, categories))
            .options(readDocumentParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .option(Options.WRITE_FILES_COMPRESSION, "zip")
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite files.
            .mode(SaveMode.Append)
            .save(path);
    }
}
