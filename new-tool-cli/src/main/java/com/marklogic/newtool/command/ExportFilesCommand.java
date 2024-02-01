package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

@Parameters(commandDescription = "Read documents from MarkLogic and write them to a local filesystem, HDFS, or S3.")
public class ExportFilesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private ReadDocumentParams readDocumentParams = new ReadDocumentParams();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--compression", description = "Set to 'ZIP' to write one zip file per partition, or to 'GZIP' to GZIP each document file.")
    private CompressionType compression;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readDocumentParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .option(Options.WRITE_FILES_COMPRESSION, compression != null ? compression.name() : null)
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite
            // files.
            .mode(SaveMode.Append)
            .save(path);
    }
}
